import * as React from 'react';
import * as Style from '../../Style';
import * as LogStyle from '../conf/LogStyle';
import * as FilterUtil from './FilterUtil';
import * as moment from 'moment';
import '../conf/LogStyle.css';
import Store from '../conf/CacheStore';
import Config from '../../Config';
import axios from 'axios';
import { Button, notification, Pagination, Popover, Tree } from 'antd';
import { GoalTaskThinBean, InceptorFlatGoalsResponse, InceptorGoalResponse, TaskBean } from '../conf/DataStructure';
import FlatListFilter from './FlatListFilter';

const TreeNode = Tree.TreeNode;

interface FlatListProps {
  date: number;
  loaded: any;
  history: any;
}

interface FlatListState {
  date: number;
  listData?: GoalTaskThinBean[];
  filterFields: any;
  page: number;
  totalNum?: number;
  filterVisible: boolean;
}

export default class FlatList extends React.Component<FlatListProps, FlatListState> {
  pageSize: number = 10;

  genTree = () => {
    const dateStyle = {
      color: '#d8d8d8',
      background: '#a3b5a0',
      fontSize: '12px',
      borderRadius: '5px',
      padding: '3px',
      paddingLeft: '5px',
      paddingRight: '5px',
      marginLeft: '5px',
      marginRight: '5px',
    };
    const sqlStyle = {
      color: '#d8d8d8',
      background: '#9c9fb5',
      fontSize: '12px',
      borderRadius: '5px',
      padding: '3px',
      paddingLeft: '5px',
      paddingRight: '5px',
      marginLeft: '5px',
      marginRight: '5px',
    };
    const indices = FilterUtil.indices(this.state.page, this.pageSize);
    return this.state.listData!.map((goalTask, id) => {
      const index = id + indices[0];
      const color = LogStyle.goalStatus(goalTask.goalStatus);
      const desc = goalTask.desc;
      const name = desc.substring(0, desc.indexOf('\n'));
      const sql = desc.substring(desc.indexOf('sql:\n') + 5, desc.indexOf('pre mark:'));
      let shortSQL = sql;
      if (shortSQL.length > 40) {
        shortSQL = shortSQL.substring(0, 40) + '...';
      }
      const popContent = (
        <div>
          <p><span style={{fontWeight: 'bold'}}>Duration: </span>{`${goalTask.duration} ms`}</p>
          <p><span style={{fontWeight: 'bold'}}>Goal Status: </span><span style={color[0]}>{color[1]}</span></p>
          <div>
            <span style={{fontWeight: 'bold'}}>SQL: </span>
            {sql.split('\n').map((value, i) => <p style={{margin: 0}} key={i}>{value}</p>)}
          </div>
        </div>
      );
      const title = (
        <Popover placement="top" title={name} content={popContent} mouseEnterDelay={0.5}>
          <div onClick={() => this.onLinkClick('goal', goalTask.id)}>
            <span style={{...{fontSize: '20px'}, ...color[0]}}>{`[${index}] ${goalTask.name}`}&nbsp;</span>
            <span style={dateStyle}>
                {moment(goalTask.startTime).format(Config.DATE_TIME_FORMAT)} ~ {moment(goalTask.endTime).format(Config.DATE_TIME_FORMAT)}
              </span>
            <span style={sqlStyle}>{shortSQL}</span>
          </div>
        </Popover>
      );
      const expandedKeys: string[] = [];
      const addExpandedKey = (key: string) => {
        expandedKeys.push(key);
      };
      addExpandedKey(index.toString());
      return (
        <Tree key={index} expandedKeys={expandedKeys} showLine={true}>
          {goalTask.tasks === null ? <TreeNode title={title} key={index.toString()}/> : (
            <TreeNode title={title} key={index.toString()}>
              {goalTask.tasks.map((node, subIndex) => this.genNode(node, goalTask.id, index.toString(), subIndex, addExpandedKey))}
            </TreeNode>
          )}
        </Tree>
      );
    });
  }

  genNode = (node: TaskBean, goalId: string, parentId: string, id: number, addExpandedKey: (key: string) => void) => {
    const currentId = parentId + '-' + id;
    const color = LogStyle.task(node.taskStatus, node.errorType);
    const statusColor = LogStyle.taskStatus(node.taskStatus);
    const errorColor = LogStyle.taskError(node.errorType);
    const classes = this.filterTask(node);
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
        <div onClick={() => this.onLinkClick('task', goalId, node)} style={color}>
          [{currentId}] {node.name}
        </div>
      </Popover>
    );
    addExpandedKey(currentId);
    if (node.subTasks === null) {
      return <TreeNode title={title} key={currentId} className={classes}/>;
    } else {
      return (
        <TreeNode title={title} key={currentId} className={classes}>
          {node.subTasks.map((subNode, index) => this.genNode(subNode, goalId, currentId, index, addExpandedKey))}
        </TreeNode>
      );
    }
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

  filterTask = (node: TaskBean) => {
    const status = node.taskStatus.toLowerCase();
    const error = node.errorType.toLowerCase();
    let type = '';
    if (status === 'complete') {
      if (error === 'no_error') {
        type = 'tc';
      } else {
        type = 'tce';
      }
    } else {
      if (error === 'no_error') {
        type = 'ti';
      } else {
        type = 'tie';
      }
    }
    let taskFilter = this.state.filterFields.task.value;
    if (taskFilter.length === 0) {
      taskFilter = ['tc', 'tce', 'ti', 'tie'];
    }
    return taskFilter.indexOf(type) < 0 ? 'node-hide' : '';
  }

  handleFormChange = (changedFields) => {
    this.setState(({filterFields}) => ({
      filterFields: {...filterFields, ...changedFields},
    }));
  }

  handleSubmit = () => {
    this.setState(({filterFields}) => ({
      page: 1, filterFields: {...filterFields, submitting: true, resetting: false}
    }));
    const fieldData = this.state.filterFields;
    const indices = FilterUtil.indices(1, this.pageSize);
    const timeRange = FilterUtil.timeRange(this.state.date, [fieldData.time.value.from, fieldData.time.value.to]);
    const durationRange = FilterUtil.duration([fieldData.duration.value.min, fieldData.duration.value.max]);
    const bean = {
      date: this.state.date,
      from: indices[0],
      to: indices[1],
      startTime: timeRange[0],
      endTime: timeRange[1],
      smartType: fieldData.smart.value ? 1 : 0,
      goalType: FilterUtil.goalType(fieldData.goal.value),
      minDuration: durationRange[0],
      maxDuration: durationRange[1],
      sortBy: fieldData.sorter.value,
      order: fieldData.order.value === 'asc',
    };
    this.changeData(bean);
  }

  handleReset = (e) => {
    e.preventDefault();
    this.setState({
      page: 1,
      filterFields: {
        sorter: {value: '2'},
        order: {value: 'desc'},
        smart: {value: false},
        goal: {value: []},
        task: {value: []},
        time: {value: {from: -28800000, to: -28800000}},
        duration: {value: {min: undefined, max: undefined}},
        submitting: false,
        resetting: true,
      },
    });
    const indices = FilterUtil.indices(1, this.pageSize);
    const bean = {
      date: this.state.date,
      from: indices[0],
      to: indices[1],
      startTime: -1,
      endTime: -1,
      smartType: 0,
      goalType: -1,
      minDuration: -1,
      maxDuration: -1,
      sortBy: '2',
      order: false,
    };
    this.changeData(bean);
  }

  onPageChange = (page) => {
    this.setState({page: page});
    const fieldData = this.state.filterFields;
    const indices = FilterUtil.indices(page, this.pageSize);
    const timeRange = FilterUtil.timeRange(this.state.date, [fieldData.time.value.from, fieldData.time.value.to]);
    const durationRange = FilterUtil.duration([fieldData.duration.value.min, fieldData.duration.value.max]);
    const bean = {
      date: this.state.date,
      from: indices[0],
      to: indices[1],
      startTime: timeRange[0],
      endTime: timeRange[1],
      smartType: fieldData.smart.value ? 1 : 0,
      goalType: FilterUtil.goalType(fieldData.goal.value),
      minDuration: durationRange[0],
      maxDuration: durationRange[1],
      sortBy: fieldData.sorter.value,
      order: fieldData.order.value === 'asc',
    };
    this.changeData(bean);
  }

  changeData = (bean) => {
    axios.post('/api/log/inceptor/flatgoals', bean, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        const response = res.data as InceptorFlatGoalsResponse;
        this.setState(({filterFields}) => ({
          listData: response.goals,
          totalNum: response.size,
          filterFields: {...filterFields, submitting: false, resetting: false},
        }));
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
        this.setState(({filterFields}) => ({
          filterFields: {...filterFields, submitting: false, resetting: false}
        }));
      });
  }

  constructor(props: any) {
    super(props);
    const rawData = Store.getState();
    if (rawData && rawData.list) {
      const timelineData = rawData.list;
      if (timelineData.hasOwnProperty(props.date)) {
        const data = timelineData[props.date];
        this.state = {
          date: props.date,
          listData: data.listData,
          filterFields: data.filterFields,
          page: data.page,
          totalNum: data.totalNum,
          filterVisible: data.filterVisible,
        };
        return;
      }
    }
    this.state = {
      date: props.date,
      filterFields: {
        sorter: {value: '2'},
        order: {value: 'desc'},
        smart: {value: false},
        goal: {value: []},
        task: {value: []},
        time: {value: {from: -28800000, to: -28800000}},
        duration: {value: {min: undefined, max: undefined}},
        submitting: false,
        resetting: false,
      },
      page: 1,
      filterVisible: false,
    };
  }

  componentDidMount() {
    if (this.state.listData !== undefined) {
      this.props.loaded();
    } else {
      const fieldData = this.state.filterFields;
      const indices = FilterUtil.indices(this.state.page, this.pageSize);
      const timeRange = FilterUtil.timeRange(this.state.date, [fieldData.time.value.from, fieldData.time.value.to]);
      const bean = {
        date: this.state.date,
        from: indices[0],
        to: indices[1],
        startTime: timeRange[0],
        endTime: timeRange[1],
        smartType: fieldData.smart.value ? 1 : 0,
        goalType: -1,
        minDuration: undefined,
        maxDuration: undefined,
        sortBy: fieldData.sorter.value,
        order: fieldData.order.value === 'asc',
      };
      axios.post('/api/log/inceptor/flatgoals', bean, {timeout: Config.REQUEST_TIMEOUT})
        .then(res => {
          const response = res.data as InceptorFlatGoalsResponse;
          this.setState({
            listData: response.goals,
            totalNum: response.size,
          });
          this.props.loaded();
        })
        .catch(err => {
          notification.error({message: 'ERROR', description: err.message, duration: 3});
          this.props.loaded();
        });
    }
  }

  componentWillUnmount() {
    const data = {
      listData: this.state.listData,
      filterFields: this.state.filterFields,
      page: this.state.page,
      totalNum: this.state.totalNum,
      filterVisible: this.state.filterVisible,
    };
    Store.dispatch({
      type: 'inceptor-list',
      date: this.state.date,
      data: data,
    });
  }

  render() {
    if (this.state.listData === undefined) {
      return <div/>;
    }
    return (
      <div>
        <Button
          style={{float: 'right'}}
          icon="filter"
          onClick={() => this.setState({filterVisible: !this.state.filterVisible})}
        >
          Filter
        </Button>
        <p style={{color: LogStyle.blue.color, fontSize: '16px', fontWeight: 'bold', float: 'left', margin: '10px'}}>
          {moment(this.state.date).format(Config.DATE_FORMAT)}
        </p>
        <div style={{clear: 'both'}}/>
        <div style={this.state.filterVisible ? {...Style.showDiv, ...LogStyle.filter} : Style.hideDiv}>
          <FlatListFilter
            {...this.state.filterFields}
            onChange={this.handleFormChange}
            onSubmit={this.handleSubmit}
            onReset={this.handleReset}
          />
        </div>
        <div style={{minHeight: '500px', marginTop: '10px'}}>
          {this.genTree()}
        </div>
        <Pagination
          style={{float: 'right', marginTop: '20px'}}
          total={this.state.totalNum}
          current={this.state.page}
          pageSize={this.pageSize}
          onChange={this.onPageChange}
          showQuickJumper={true}
        />
      </div>
    );
  }
}
