import * as React from 'react';
import * as LogStyle from '../conf/LogStyle';
import * as moment from 'moment';
import * as echarts from 'echarts';
import * as TaskTimeline from './TaskTimeLine';
import Config from '../../Config';
import axios from 'axios';
import vis from 'vis/dist/vis';
import 'vis/dist/vis.css';
import { BackTop, Button, Collapse, Icon, List, notification, Popover, Tree } from 'antd';
import { InceptorGoalResponse, LogEntity, TaskBean } from '../conf/DataStructure';
import { HashLink as Link } from 'react-router-hash-link';

const Panel = Collapse.Panel;
const TreeNode = Tree.TreeNode;

interface TaskState {
  date: number;
  goalId: string;
  node: TaskBean;
  entities: LogEntity[];
  itemtaskDataSet?: vis.DataSet;
  taskitems?: {};
}

export default class Task extends React.Component<any, TaskState> {
  myChart: any;
  link: any = [];
  data: any = [];
  tag: any = [];

  showExceptions = () => {
    const renderItem = (value: number, index: number) => {
      const link = (
        <Link
          to={{pathname: '/log/inceptor/logs', hash: value.toString(), state: {entities: this.state.entities}}}
          key={'link-' + index}
          smooth={true}
        >
          <Icon type="right-circle-o"/>
        </Link>
      );
      return (
        <List.Item key={'err-' + index} actions={[link]}>
          <div style={LogStyle.wrap}>
            {this.state.entities[value].content.split('\n').map((v, id) =>
              <p style={{margin: 0}} key={id}>{v}</p>
            )}
          </div>
        </List.Item>
      );
    };
    return (
      <Panel header="Exceptions" key="panel-1" style={{background: 'rgba(255,0,0,0.2)'}}>
        <List dataSource={this.state.node.errorIndices} renderItem={renderItem}/>
      </Panel>
    );
  }

  showLogs = () => {
    const indices: number[] = [];
    for (let i = this.state.node.preIndex; i <= this.state.node.postIndex; i++) {
      indices.push(i);
    }
    const renderItem = (value: number, index: number) => {
      const entity = this.state.entities[value];
      const link = (
        <Link
          to={{pathname: '/log/inceptor/logs', hash: value.toString(), state: {entities: this.state.entities}}}
          key={'link-' + index}
          smooth={true}
        >
          <Icon type="right-circle-o"/>
        </Link>
      );
      return (
        <List.Item key={'all-' + index} style={LogStyle.entity(entity.level)} actions={[link]}>
          <div style={LogStyle.wrap}>
            {entity.content.split('\n').map((v, id) =>
              <p style={{margin: 0}} key={id}>{v}</p>
            )}
          </div>
        </List.Item>
      );
    };
    return (
      <Panel header="Log Entries" key="panel-2" style={{background: 'rgba(0,255,0,0.2)'}}>
        <List dataSource={indices} renderItem={renderItem}/>
      </Panel>
    );
  }

  renderState = () => {
    const color = LogStyle.taskStatus(this.state.node.taskStatus);
    return (
      <p style={{display: 'inline-block', marginLeft: '20px', marginRight: '20px'}}>
        Task Status: <span style={color[0]}>{color[1]}</span>
      </p>
    );
  }

  renderErrorType = () => {
    const color = LogStyle.taskError(this.state.node.errorType);
    return (
      <p style={{display: 'inline-block', marginLeft: '20px', marginRight: '20px'}}>
        Error Type: <span style={color[0]}>{color[1]}</span>
      </p>
    );
  }

  genTree = () => {
    const index = '0';
    const color = LogStyle.taskStatus(this.state.node.taskStatus);
    const popContent = (
      <div>
        <p><span style={{fontWeight: 'bold'}}>Duration: </span>{`${this.state.node.duration} ms`}</p>
        <p><span style={{fontWeight: 'bold'}}>Task Status: </span><span style={color[0]}>{color[1]}</span></p>
      </div>
    );
    const title = (
      <Popover placement="top" title={this.state.node.name} content={popContent} mouseEnterDelay={0.5}>
        <div style={{cursor: 'default'}}>
            <span style={{...{fontSize: '15px'}}}>
              {`[${index}] ${this.state.node.name}`}&nbsp;
            </span>
          <span style={{...{fontSize: '15px'}}}>
              {`(dur: ${this.state.node.duration} ms)`}&nbsp;
            </span>
        </div>
      </Popover>
    );
    return (
      <Tree defaultExpandAll={true} showLine={true}>
        {this.state.node.subTasks === null ? <TreeNode title={title} key={index}/> : (
          <TreeNode title={title} key={index}>
            {this.state.node.subTasks.map((node, subIndex) => this.genNode(node, '0', subIndex))}
          </TreeNode>
        )}
      </Tree>
    );
  }

  genNode = (node: TaskBean, parentId: string, id: number) => {
    const currentId = parentId + '-' + id;
    const Id = this.state.goalId;
    const date = this.state.date;
    const entity = this.state.entities;
    const status = node.taskStatus;
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
    const title = (
      <Popover placement="top" title={node.desc.split('\n')[0]} content={popContent} mouseEnterDelay={0.5}>
        <Link
          to={{pathname: '/log/inceptor/task', state: {node: node, entities: entity, date: date, id: Id}}}
          style={LogStyle.task(status, node.errorType)}
        >
          {currentId + ': ' + node.name + '(dur:' + node.duration + ' ms)'}
        </Link>
      </Popover>
    );
    if (node.subTasks === null) {
      return <TreeNode title={title} key={currentId}/>;
    } else {
      return (
        <TreeNode title={title} key={currentId}>
          {node.subTasks.map((subNode, index) => this.genNode(subNode, currentId, index))}
        </TreeNode>
      );
    }
  }

  genTotalLevel = (item) => {
    var totalLevel = 0;
    if (item.subTasks !== null) {
      totalLevel += 1;
      item.subTasks.forEach((task) => {
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

  genDataAndLink = (item, tasks, tag, num, mark, index, currentLevel, totalLevel, type) => {
    var targetValue: any;
    var sourcePercent: any;
    var targetPercent: any;
    var sourceName: any;
    var targetName: any;
    if (type === 'unknown') {
      tag += 1;
      num += 1;
      if (index === 0) {
        targetValue = tasks[index].startTime - item.startTime;
      } else {
        targetValue = tasks[index].startTime - tasks[index - 1].endTime;
      }
      targetPercent = (100 * (targetValue / this.data[0].value)).toFixed(2);
      targetName = 'L' + currentLevel + ': ' + 'Unknown-' + num + ' (' + targetPercent + '%)';
      if (mark === 0) {
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
      if (mark === 0) {
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
        if (mark === 0) {
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

  genChartData = (item, tasks, tag, num, mark, type, currentLevel, totalLevel) => {
    var obj: any = {tag, num};
    if (currentLevel === 1) {
      this.data.push({
        name: 'L0: ' + item.name + ' (100%)',
        value: item.duration,
        itemStyle: {color: '#B0C4DE'},
      });
      this.link.push({
        source: null,
        target: 'L0: ' + item.name + ' (100%)',
        value: item.duration,
      });
      this.tag.push({tag: -1});
    }
    if (tasks !== null) {
      tasks.forEach((task, index) => {
        if (index === 0) {
          if (item.startTime !== tasks[index].startTime) {
            obj = this.genDataAndLink(item, tasks, tag, num, mark, index, currentLevel, totalLevel, 'unknown');
            tag = obj.tag;
            num = obj.num;
          }
          obj = this.genDataAndLink(item, tasks, tag, num, mark, index, currentLevel, totalLevel, 'known');
          tag = obj.tag;
          num = obj.num;
        } else {
          if (tasks[index - 1].endTime !== tasks[index].startTime) {
            obj = this.genDataAndLink(item, tasks, tag, num, mark, index, currentLevel, totalLevel, 'unknown');
            tag = obj.tag;
            num = obj.num;
          }
          obj = this.genDataAndLink(item, tasks, tag, num, mark, index, currentLevel, totalLevel, 'known');
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
    var obj = this.genChartData(item, item.subTasks, 0, 0, 0, 'task', currentLevel, totalLevel);
    var tag = obj.tag;
    var num = obj.num;

    if (item.subTasks !== null) {
      currentLevel += 1;
      item.subTasks.forEach((task1, index1) => {
        obj = this.genChartData(task1, task1.subTasks, tag, num, 1, 'task', currentLevel, totalLevel);
        tag = obj.tag;
        num = obj.num;
        subtask = task1;
        var mark = 0;
        while (subtask.subTasks !== null && mark === 0) {
          currentLevel += 1;
          subtask.subTasks.forEach((task2, index2) => {
            obj = this.genChartData(task2, task2.subTasks, tag, num, 1, 'task', currentLevel, totalLevel);
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
          right: '60%',
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
        if (param.data.name.substring(param.data.name.indexOf('-') + 2, param.data.name.indexOf('-')) === 'Unknown') {
          return;
        }
      } else if (param.dataType === 'edge') {
        index = param.dataIndex - this.tag[param.dataIndex + 1].tag;
        if (param.data.target.substring(param.data.target.indexOf('-') + 2, param.data.target.indexOf('-')) === 'Unknown') {
          return;
        }
      }
      const item = this.state.itemtaskDataSet.get(index);

      if (item === null) {
        return;
      }
      if (this.state.node.subTasks !== null || (item.id !== null)) {
        axios.post('/api/log/inceptor/goal', {goalID: this.state.goalId, date: this.state.date}, {timeout: Config.REQUEST_TIMEOUT})
          .then(res => {
            this.props.history.push({
              pathname: '/log/inceptor/task',
              state: {
                node: this.state.node.subTasks[item.id],
                entities: (res.data as InceptorGoalResponse).goal.entities,
                date: this.state.date,
                id: this.state.goalId,
              }
            });
          })
          .catch(err => {
            notification.error({message: 'ERROR', description: err.message, duration: 3});
          });
      }
    }
  }

  constructor(props: any) {
    super(props);
    this.state = {
      date: props.location.state.date,
      goalId: props.location.state.id,
      node: props.location.state.node,
      entities: props.location.state.entities
    };
  }

  componentWillReceiveProps(props: any) {
    if (props.location.state.node !== undefined) {
      this.setState({
        node: props.location.state.node,
        entities: props.location.state.entities
      });
    }
  }

  componentDidUpdate() {
    if (this.state.node.subTasks !== null) {
      this.showSankey(this.state.node);
    }
  }

  componentDidMount() {
    window.scrollTo(0, 0);
    const taskdata = TaskTimeline.tasktimelineData(this.state.node);
    const taskdataSet = TaskTimeline.dataSet(taskdata.items, this.state.node);
    this.setState({
      taskitems: taskdata.items,
      itemtaskDataSet: taskdataSet[0],
    });
    if (this.state.node.subTasks !== null) {
      this.showSankey(this.state.node);
    }
  }

  render() {
    return (
      <div style={LogStyle.page}>
        <BackTop/>
        <Link to={{pathname: '/log/inceptor/logs', state: {entities: this.state.entities}}}>
          <Button type="primary" style={{display: 'inline-block', float: 'right'}}>All Logs</Button>
        </Link>
        <div>
          <h1 style={{display: 'inline-block'}}>{this.state.node.name}</h1>
          {this.renderState()}
          {this.renderErrorType()}
        </div>
        <p style={{marginLeft: '20px'}}>
          <span style={{fontWeight: 'bold'}}>{this.state.node.duration} ms: </span>
          {moment(this.state.node.startTime).format(Config.DATE_TIME_FORMAT)} ~ {moment(this.state.node.endTime).format(Config.DATE_TIME_FORMAT)}
        </p>
        <div style={{marginLeft: '20px'}}>
          <span style={{fontWeight: 'bold'}}>Desc: </span>
          {this.state.node.desc.split('\n').map((value, index) => <p style={{margin: 0}} key={index}>{value}</p>)}
        </div>
        {this.state.node.subTasks !== null && (
          <div style={{marginTop: '20px'}}>
            <h3>Sub Tasks:</h3>
            {this.genTree()}
            <h3 style={{marginTop: '20px'}}>Tasks Sankey:</h3>
            <div id="statusChangeChart" style={{height: '300px'}}/>
          </div>
        )}
        <Collapse defaultActiveKey={['panel-1']} style={{marginTop: '20px'}}>
          {this.state.node.errorIndices === null ? '' : this.showExceptions()}
          {this.showLogs()}
        </Collapse>
      </div>
    );
  }
}
