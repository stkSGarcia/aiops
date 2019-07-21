import * as React from 'react';
import * as LogStyle from '../conf/LogStyle';
import * as TimelineUtil from './TimelineUtil';
import * as GoalTimeline from './GoalTimeLine';
import * as FilterUtil from './FilterUtil';
import * as moment from 'moment';
import * as echarts from 'echarts';
import Config from '../../Config';
import axios from 'axios';
import vis from 'vis/dist/vis';
import 'vis/dist/vis.css';
import { GoalTaskBean, InceptorGoalResponse, InceptorGoalTimelineResponse, LogEntity, TaskBean } from '../conf/DataStructure';
import { BackTop, Collapse, List, notification, Pagination, Popover, Tree } from 'antd';
import { Link } from 'react-router-dom';

const Panel = Collapse.Panel;
const TreeNode = Tree.TreeNode;

interface GoalState {
  date: number;
  goal: GoalTaskBean;
  items?: {};
  taskitems?: {};
  groups?: any[];
  itemDataSet?: vis.DataSet;
  itemTaskDataSet?: vis.DataSet;
  page: number;
  pageSize: number;
  totalNum?: number;
  task: TaskBean;
}

export default class Goal extends React.Component<any, GoalState> {
  timeline: any;
  myChart: any;
  link: any = [];
  data: any = [];
  tag: any = [];

  genTotalLevel = (item) => {
    var totalLevel = 0;
    if (item.tasks !== null) {
      totalLevel += 1;
      item.tasks.forEach((task) => {
        var subtasks = task;
        var mark = 0;
        while (subtasks.subTasks !== null && mark === 0) {
          totalLevel += 1;
          mark = 1;
          subtasks.subTasks.forEach((subtask) => {
            if (subtask.subTasks !== null) {
              subtasks = subtask.subTasks;
              totalLevel += 1;
              mark = 0;
            }
          });
        }
      });
    }
    return totalLevel;
  }

  genDataAndLink = (item, tasks, tag, num, type, index, currentLevel, totalLevel, kind) => {
    var targetValue: any;
    var sourcePercent: any;
    var targetPercent: any;
    var sourceName: any;
    var targetName: any;
    if (kind === 'unknown') {
      tag += 1;
      num += 1;

      if (index === 0) {
        targetValue = tasks[index].startTime - item.startTime;
      } else {
        targetValue = tasks[index].startTime - tasks[index - 1].endTime;
      }
      targetPercent = (100 * (targetValue / this.data[0].value)).toFixed(2);
      targetName = 'L' + currentLevel + ': ' + 'Unknown-' + num + ' (' + targetPercent + '%)';
      if (type === 'goal') {
        sourceName = 'L0: ' + item.name + ' (100%)';
      } else {
        sourcePercent = (100 * (item.duration / this.data[0].value)).toFixed(2);
        sourceName = 'L' + (currentLevel - 1) + ': ' + item.name + ' (' + sourcePercent + '%)';
      }
      this.data.push({
        name: targetName,
        value: targetValue,
      });
      this.link.push({
        source: sourceName,
        target: targetName,
        value: targetValue,
      });
      this.tag.push({tag: tag});
      var count = currentLevel;
      while (count < totalLevel) {
        count += 1;
        sourceName = 'L' + (count - 1) + ': ' + 'Unknown-' + num + ' (' + targetPercent + '%)';
        targetName = 'L' + count + ': ' + 'Unknown-' + num + ' (' + targetPercent + '%)';
        this.data.push({
          name: targetName,
          value: targetValue,
        });
        this.link.push({
          source: sourceName,
          target: targetName,
          value: targetValue,
        });
        tag += 1;
        this.tag.push({tag: tag});
      }
    } else {
      var color: any;
      if (tasks[index].errorType === 'NO_ERROR' && tasks[index].taskStatus === 'COMPLETE') {
        color = '#00b200';
      } else {
        color = '#c40000';
      }
      targetValue = tasks[index].duration;
      targetPercent = (100 * (targetValue / this.data[0].value)).toFixed(2);
      targetName = 'L' + currentLevel + ': ' + tasks[index].name + ' (' + targetPercent + '%)';
      if (type === 'goal') {
        sourceName = 'L0: ' + item.name + ' (100%)';
      } else {
        sourcePercent = (100 * (item.duration / this.data[0].value)).toFixed(2);
        sourceName = 'L' + (currentLevel - 1) + ': ' + item.name + ' (' + sourcePercent + '%)';
      }
      this.data.push({
        name: targetName,
        value: targetValue,
        label: {color: color},
      });
      this.link.push({
        source: sourceName,
        target: targetName,
        value: targetValue,
      });
      this.tag.push({tag: tag});
      if (tasks[index].subTasks === null) {
        var count1 = currentLevel;
        while (count1 < totalLevel) {
          count1 += 1;
          targetValue = tasks[index].duration;
          targetPercent = (100 * (targetValue / this.data[0].value)).toFixed(2);
          sourceName = 'L' + (count1 - 1) + ': ' + tasks[index].name + ' (' + targetPercent + '%)';
          targetName = 'L' + count1 + ': ' + tasks[index].name + ' (' + targetPercent + '%)';
          this.data.push({
            name: targetName,
            value: targetValue,
            label: {color: color},
          });
          this.link.push({
            source: sourceName,
            target: targetName,
            value: targetValue,
          });
          tag += 1;
          this.tag.push({tag: tag});
        }
      }
      const length = tasks.length - 1;
      if (index === length && tasks[index].endTime !== item.endTime) {
        tag += 1;
        num += 1;
        targetValue = item.endTime - tasks[index].endTime;
        targetPercent = (100 * (targetValue / this.data[0].value)).toFixed(2);
        targetName = 'L' + currentLevel + ': ' + 'Unknown-' + num + ' (' + targetPercent + '%)';
        if (type === 'goal') {
          sourceName = 'L0: ' + item.name + ' (100%)';
        } else {
          sourcePercent = (100 * (item.duration / this.data[0].value)).toFixed(2);
          sourceName = 'L' + (currentLevel - 1) + ': ' + item.name + ' (' + sourcePercent + '%)';
        }
        this.data.push({
          name: targetName,
          value: targetValue,
        });
        this.link.push({
          source: sourceName,
          target: targetName,
          value: targetValue,
        });
        this.tag.push({tag: tag});
      }
    }
    return {tag, num};
  }

  genChartData = (item, tasks, tag, num, type, currentLevel, totalLevel) => {
    var obj: any = {tag, num};
    if (type === 'goal') {
      this.data.push({
        name: 'L0: ' + item.name + ' (100%)',
        value: item.duration,
        itemStyle: {color: '#B0C4DE'},
      });
      this.tag.push({tag: -1});
    }
    if (tasks === null) {
      if (type === 'goal') {
        this.link.push({
          source: null,
          target: 'L0: ' + item.name + ' (100%)',
          value: item.duration,
        });
        this.tag.push({tag: tag});
      }
    } else {
      tasks.forEach((task, index) => {
        if (index === 0) {
          if (item.startTime !== tasks[index].startTime) {
            obj = this.genDataAndLink(item, tasks, tag, num, type, index, currentLevel, totalLevel, 'unknown');
            tag = obj.tag;
            num = obj.num;
          }
          obj = this.genDataAndLink(item, tasks, tag, num, type, index, currentLevel, totalLevel, 'known');
          tag = obj.tag;
          num = obj.num;
        } else {
          if (tasks[index - 1].endTime !== tasks[index].startTime) {
            obj = this.genDataAndLink(item, tasks, tag, num, type, index, currentLevel, totalLevel, 'unknown');
            tag = obj.tag;
            num = obj.num;
          }
          obj = this.genDataAndLink(item, tasks, tag, num, type, index, currentLevel, totalLevel, 'known');
          tag = obj.tag;
          num = obj.num;
        }
      });
    }
    return {tag, num};
  }

  showSankey = (item) => {
    var totalLevel = this.genTotalLevel(item);
    this.link = [];
    this.data = [];
    this.tag = [];
    var subtask: any = [];
    var currentLevel = 1;
    var obj = this.genChartData(item, item.tasks, 0, 0, 'goal', currentLevel, totalLevel);
    var tag = obj.tag;
    var num = obj.num;

    if (item.tasks !== null) {
      currentLevel += 1;
      item.tasks.forEach((task1, index1) => {
        obj = this.genChartData(task1, task1.subTasks, tag, num, 'task', currentLevel, totalLevel);
        tag = obj.tag;
        num = obj.num;
        subtask = task1;
        var mark = 0;
        while (subtask.subTasks !== null && mark === 0) {
          currentLevel += 1;
          subtask.subTasks.forEach((task2, index2) => {
            obj = this.genChartData(task2, task2.subTasks, tag, num, 'task', currentLevel, totalLevel);
            tag = obj.tag;
            num = obj.num;
            mark = 1;
            if (task2.subTasks !== null) {
              subtask = task2.subTasks;
              mark = 0;
            }
          });
        }
      });
    }

    var option = {
      tooltip: {
        show: 'true',
        formatter: function (params: any) {
          var relVal: any;
          var showInfo = '<br>' + 'Duration: ' + params.value + ' ms';
          if (params.dataType === 'node') {
            relVal = params.data.name + showInfo;
          } else if (params.dataType === 'edge') {
            relVal = params.data.source + ' - ' + params.data.target + showInfo;
          }
          return relVal;
        }
      },
      legend: {
        data: this.data,
        x: 'center',
        y: 'bottom',
      },
      calculable: false,
      series: [
        {
          type: 'sankey',
          left: '10%',
          right: '40%',
          nodeGap: 12,
          layout: 'none',
          layoutIterations: 0,
          data: this.data,
          links: this.link,
          avoidLabelOverlap: true,
          draggable: false,
          lineStyle: {
            normal: {
              color: 'source',
              curveness: 0.5,
            }
          }
        }],
    };
    this.myChart = echarts.init(document.getElementById('statusChangeChart'));
    this.myChart.setOption(option);
    this.myChart.on('click', this.eConsole);
  }

  eConsole = (param) => {
    if (typeof param.seriesIndex === 'undefined') {
      return;
    }
    if (param.type === 'click') {
      var index = 0;
      if (param.dataType === 'node') {
        index = param.dataIndex - 1 - this.tag[param.dataIndex].tag;
        if (param.dataIndex === 0) {
          return;
        }
        if (param.data.name.substring(param.data.name.indexOf(':') + 2, param.data.name.indexOf('-')) === 'Unknown') {
          return;
        }
      } else if (param.dataType === 'edge') {
        index = param.dataIndex - this.tag[param.dataIndex + 1].tag;
        if (param.data.target.substring(param.data.target.indexOf(':') + 2, param.data.target.indexOf('-')) === 'Unknown') {
          return;
        }
      }
      const item = this.state.itemTaskDataSet.get(index);
      if (item === null) {
        return;
      }
      if (this.state.goal.tasks !== null || (item.id !== null)) {
        var node: any = null;
        if (item.key.indexOf('-') === -1) {
          node = this.state.goal.tasks[item.id];
        } else {
          var a: any = [];
          var num: any = 0;
          a = item.key.split('-');
          if (a.length === 1) {
            node = this.state.goal.tasks[item.id];
          } else {
            node = this.state.goal.tasks[a[0]];
            num += 1;
            while (num < a.length) {
              node = node.subTasks[a[num]];
              num += 1;
            }
          }
        }
        this.props.history.push({
          pathname: '/log/inceptor/task',
          state: {
            node: node,
            entities: this.state.goal.entities,
            date: this.state.date,
            id: item.group,
          }
        });
      }
    }
  }

  renderStatus = () => {
    const color = LogStyle.goalStatus(this.state.goal.goalStatus);
    return (
      <p style={{display: 'inline-block', marginLeft: '20px', marginRight: '20px'}}>
        <span style={color[0]}>{color[1]}</span>
      </p>
    );
  }

  showTimeline = (items, groups, min, max) => {
    const container = document.getElementById('inceptor-goal-timeline');
    const option = {
      orientation: 'top',
      minHeight: 600,
      horizontalScroll: true,
      zoomKey: 'ctrlKey',
      min: moment(min),
      max: moment(max),
      start: moment(this.state.goal.startTime),
      end: moment(this.state.goal.endTime),
      // zoomMax: 3600000,
      stack: false,
      tooltip: {
        followMouse: true,
      }
    };
    this.timeline = new vis.Timeline(container, items, groups, option);
  }

  genTree = () => {
    const index = '0';
    const color = LogStyle.goalStatus(this.state.goal.goalStatus);
    const desc = this.state.goal.desc.split('\n');
    let shortSQL = desc[2];
    if (shortSQL.length > 40) {
      shortSQL = shortSQL.substring(0, 40) + '...';
    }
    const popContent = (
      <div>
        <p><span style={{fontWeight: 'bold'}}>Duration: </span>{`${this.state.goal.duration} ms`}</p>
        <p><span style={{fontWeight: 'bold'}}>Goal Status: </span><span style={color[0]}>{color[1]}</span></p>
        <p><span style={{fontWeight: 'bold'}}>SQL: </span>{shortSQL}</p>
      </div>
    );
    const title = (
      <Popover placement="top" title={this.state.goal.name} content={popContent} mouseEnterDelay={0.5}>
        <div onClick={() => this.onLinkClick('goal', this.state.goal.id)} style={{cursor: 'default'}}>
            <span style={{...{fontSize: '15px'}}}>
              {`[${index}] ${this.state.goal.name}`}&nbsp;
            </span>
          <span style={{...{fontSize: '15px'}}}>
              {`(dur: ${this.state.goal.duration} ms)`}&nbsp;
            </span>
        </div>
      </Popover>
    );
    return (
      <Tree defaultExpandAll={true} showLine={true}>
        {this.state.goal.tasks === null ? <TreeNode title={title} key={index}/> : (
          <TreeNode title={title} key={index}>
            {this.state.goal.tasks.map((node, subIndex) => this.genNode(node, '0', subIndex))}
          </TreeNode>
        )}
      </Tree>
    );

  }

  genNode = (node: TaskBean, parentId: string, id: number) => {
    const currentId = parentId + '-' + id;
    const statusColor = LogStyle.taskStatus(node.taskStatus);
    const errorColor = LogStyle.taskError(node.errorType);
    const popContent = (
      <div>
        <p>
          <span style={{fontWeight: 'bold'}}>Duration: </span>
          {node.duration} ms&nbsp;
          ({moment(node.startTime).format(Config.DATE_TIME_FORMAT)} ~ {moment(node.endTime).format(Config.DATE_TIME_FORMAT)})
        </p>
        <p><span style={{fontWeight: 'bold'}}>Task Status: </span><span style={statusColor[0]}>{statusColor[1]}</span></p>
        <p><span style={{fontWeight: 'bold'}}>Error Type: </span><span style={errorColor[0]}>{errorColor[1]}</span></p>
      </div>
    );
    const Id = this.state.goal.id;
    const date = this.state.date;
    const entity = this.state.goal.entities;
    const status = node.taskStatus;
    const title = (
      <Popover placement="top" title={node.desc.split('\n')[0]} content={popContent} mouseEnterDelay={0.5}>
        <Link
          to={{pathname: '/log/inceptor/task', state: {node: node, entities: entity, date: date, id: Id}}}
          style={LogStyle.task(status, node.errorType)}
        >
          {currentId + ': ' + node.name + `(dur: ${node.duration} ms)`}
        </Link>
      </Popover>

    );
    return <TreeNode title={title} key={currentId}/>;
  }

  onLinkClick = (type: string, goalId: string, task?: TaskBean) => {
    axios.post('/api/log/inceptor/goal', {goalID: goalId, date: this.state.date}, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        switch (type) {
          case 'goal':
            this.props.history.push({
              pathname: '/log/inceptor/goal',
              state: {
                goal: (res.data as InceptorGoalResponse).goal,
                date: this.state.date,
              }
            });
            break;
          case 'task':
            this.props.history.push({
              pathname: '/log/inceptor/task',
              state: {
                node: task,
                entities: (res.data as InceptorGoalResponse).goal.entities,
                date: this.state.date,
                id: goalId,
              }
            });
            break;
          default:
            notification.error({message: 'ERROR', description: 'Unsupported type', duration: 3});
        }
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  showLogs = () => {
    const renderItem = (entity: LogEntity, index: number) => (
      <List.Item key={'all-' + index} style={LogStyle.entity(entity.level)}>
        <div style={LogStyle.wrap}>
          {entity.content.split('\n').map((value, id) =>
            id === 0 ? <p style={{margin: 0}} key={id}>{index + 1}: {value}</p> :
              <p style={{margin: 0}} key={id}>{value}</p>
          )}
        </div>
      </List.Item>
    );
    return (
      <Collapse style={{marginTop: '20px'}}>
        <Panel header="All Logs" key="log-panel" style={{background: 'rgba(0,255,0,0.2)'}}>
          <List dataSource={this.state.goal.entities} renderItem={renderItem}/>
        </Panel>
      </Collapse>
    );
  }

  onItemClick = (event) => {
    const props = this.timeline.getEventProperties(event);
    if (props.what !== 'item') {
      return;
    }
    const item = this.state.itemDataSet.get(props.item);
    if (item.id === this.state.goal.id) {
      return;
    }
    axios.post('/api/log/inceptor/goal', {goalID: item.id, date: this.state.date}, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        this.props.history.push({
          pathname: '/log/inceptor/goal',
          state: {
            goal: (res.data as InceptorGoalResponse).goal,
            date: this.state.date,
          }
        });
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  onPageChange = (page) => {
    this.setState({page: page});
    const indices = FilterUtil.indices(page, this.state.pageSize);
    const bean = {
      goalID: this.state.goal.id,
      date: this.state.date,
      from: indices[0],
      to: indices[1],
    };
    this.changeData(bean);
  }

  changeData = (bean) => {
    axios.post('/api/log/inceptor/goaltimeline', bean, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        const response = res.data as InceptorGoalTimelineResponse;
        const data = TimelineUtil.timelineData(response.goalsBySession, bean.from, response.targetSession, this.state.goal.id);
        const dataSet = TimelineUtil.dataSet(data.items, data.groups);
        this.setState({
          items: data.items,
          groups: data.groups,
          itemDataSet: dataSet[0],
          totalNum: response.size,
        });
        this.timeline.setData({
          items: dataSet[0],
          groups: dataSet[1],
        });
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  constructor(props: any) {
    super(props);
    this.state = {
      date: props.location.state.date,
      goal: props.location.state.goal,
      page: 1,
      pageSize: 20,
      task: props.location.state.goal.tasks
    };
    const indices = FilterUtil.indices(1, this.state.pageSize);
    const bean = {
      goalID: this.state.goal.id,
      date: this.state.date,
      from: indices[0],
      to: indices[1],
    };
    axios.post('/api/log/inceptor/goaltimeline', bean, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        const response = res.data as InceptorGoalTimelineResponse;
        const data = TimelineUtil.timelineData(response.goalsBySession, bean.from, response.targetSession, this.state.goal.id);
        const dataSet = TimelineUtil.dataSet(data.items, data.groups);
        this.setState({
          items: data.items,
          groups: data.groups,
          itemDataSet: dataSet[0],
          totalNum: response.size,
        });
        this.showTimeline(dataSet[0], dataSet[1], data.min, data.max);
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  componentWillReceiveProps(props: any) {
    const newDate = props.location.state.date;
    const newGoal = props.location.state.goal;
    this.setState({
      date: newDate,
      goal: newGoal,
      page: 1,
    });
    const indices = FilterUtil.indices(1, this.state.pageSize);
    const bean = {
      goalID: newGoal.id,
      date: newDate,
      from: indices[0],
      to: indices[1],
    };
    axios.post('/api/log/inceptor/goaltimeline', bean, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        const response = res.data as InceptorGoalTimelineResponse;
        const data = TimelineUtil.timelineData(response.goalsBySession, bean.from, response.targetSession, this.state.goal.id);
        const dataSet = TimelineUtil.dataSet(data.items, data.groups);
        this.setState({
          items: data.items,
          groups: data.groups,
          itemDataSet: dataSet[0],
          totalNum: response.size,
        });
        this.timeline.setData({
          items: dataSet[0],
          groups: dataSet[1],
        });
        this.timeline.setOptions({
          min: moment(data.min),
          max: moment(data.max),
        });
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
    const goaldata = GoalTimeline.goaltimelineData(this.state.goal);
    const goaldataSet = GoalTimeline.dataSet(goaldata.items, this.state.goal);
    this.setState({
      taskitems: goaldata.items,
      itemTaskDataSet: goaldataSet[0],
    });
  }

  componentDidUpdate() {
    if (this.state.goal.tasks !== null && this.state.goal.duration !== 0) {
      this.showSankey(this.state.goal);
    }
  }

  componentDidMount() {
    window.scrollTo(0, 0);
    const goaldata = GoalTimeline.goaltimelineData(this.state.goal);
    const goaldataSet = GoalTimeline.dataSet(goaldata.items, this.state.goal);
    this.setState({
      taskitems: goaldata.items,
      itemTaskDataSet: goaldataSet[0],
    });
    if (this.state.goal.tasks !== null && this.state.goal.duration !== 0) {
      this.showSankey(this.state.goal);
    }
  }

  render() {
    return (
      <div style={LogStyle.page}>
        <BackTop/>
        <div>
          <h1 style={{display: 'inline-block'}}>{this.state.goal.name}</h1>
          {this.renderStatus()}
        </div>
        {this.state.goal.duration !== null && (
          <p style={{marginLeft: '20px', marginTop: '10px'}}>
            <span style={{fontWeight: 'bold'}}>{this.state.goal.duration} ms: </span>
            {moment(this.state.goal.startTime).format(Config.DATE_TIME_FORMAT)} ~ {moment(this.state.goal.endTime).format(Config.DATE_TIME_FORMAT)}
          </p>
        )}
        <p style={{marginLeft: '20px'}}><span style={{fontWeight: 'bold'}}>Status Message: </span>{this.state.goal.statusMsg}</p>
        <div style={{marginLeft: '20px'}}>
          <span style={{fontWeight: 'bold'}}>Desc: </span>
          {this.state.goal.desc.split('\n').map((value, index) => <p style={{margin: 0}} key={index}>{value}</p>)}
        </div>
        {(this.state.goal.tasks !== null && this.state.goal.duration !== 0) && (
          <div>
            <div style={{marginTop: '10px'}}>
              <h3>Goal-Tasks: </h3>
              {this.genTree()}
            </div>
            <h3 style={{marginTop: '20px'}}>Tasks Sankey:</h3>
            <div id="statusChangeChart" style={{width: '100%', height: '300px'}}/>
          </div>
        )}
        <h3 style={{marginTop: '20px'}}>Nearby Goals:</h3>
        <div id="inceptor-goal-timeline" style={{marginTop: '20px'}} onClick={this.onItemClick}/>
        <Pagination
          style={{float: 'right', marginTop: '20px'}}
          total={this.state.totalNum}
          current={this.state.page}
          pageSize={this.state.pageSize}
          onChange={this.onPageChange}
          showQuickJumper={true}
        />
        <div style={{clear: 'both'}}/>
        {this.state.goal.entities !== null && this.showLogs()}
      </div>
    );
  }
}
