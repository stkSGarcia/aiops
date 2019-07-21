import * as React from 'react';
import * as Style from '../Style';
import * as JstackStyle from './conf/JstackStyle';
import './conf/JstackStyle.css';
import Store from './conf/CacheStore';
import ReactEcharts from 'echarts-for-react';
import { List, Table } from 'antd';
import { EntryMap, JstackEntry, JstackEntryWithLevel, JstackFileInfo, LockMap } from './conf/DataStructure';
import { Link } from 'react-router-dom';

interface FileDashboardProps {
  fileInfo: JstackFileInfo;
  fileIndex: number;
  history: any;
  locationKey: string;
}

interface FileDashboardState {
  timestamp: string;
  entryMap: EntryMap;
  lockMap: LockMap;
  statistic: any[];
  stateMap: any;
  traceData: any[];
  traceOrder: any;
  traceFilter: string[];
  tracePage: number;
  methodData: any[];
  methodOrder: any;
  methodPage: number;
  groupData: any[];
  groupOrder: any;
  groupPage: number;
}

export default class FileDashboard extends React.Component<FileDashboardProps, FileDashboardState> {
  foldLength = 5;

  renderItem = (item: string) => {
    if (item.length > 30) {
      item = item.slice(0, 29);
    }
    return <List.Item>{item}</List.Item>;
  }

  genData = (rawData: JstackFileInfo) => {
    const callStackMap = rawData.callStackMap;
    const noCallStackArray = rawData.noCallStackArray;
    const methodList = rawData.methodList;
    const groupMap = rawData.groupMap;
    const stateMap = {};
    const traceData: any[] = [];
    const methodData: any[] = [];
    const groupData: any[] = [];
    const entryMap = {};
    let total = 0;
    const newThread: JstackEntry[] = [];
    const runnable: JstackEntry[] = [];
    const blocked: JstackEntry[] = [];
    const waiting: JstackEntry[] = [];
    const timedWaiting: JstackEntry[] = [];
    const terminated: JstackEntry[] = [];
    const usefulCallStackMap = {};
    const usefulGroupMap = {};

    const classify = (entry: JstackEntry) => {
      switch (entry.threadState.value.toUpperCase()) {
        case 'NEW':
          newThread.push(entry);
          break;
        case 'RUNNABLE':
          runnable.push(entry);
          break;
        case 'BLOCKED':
          blocked.push(entry);
          break;
        case 'WAITING':
          waiting.push(entry);
          break;
        case 'TIMED_WAITING':
          timedWaiting.push(entry);
          break;
        case 'TERMINATED':
          terminated.push(entry);
          break;
        default:
          break;
      }
    };

    Object.keys(callStackMap).forEach((value, index) => {
      const entries: JstackEntry[] = callStackMap[value].entryList;
      entries.forEach(entry => {
        total++;
        entryMap[entry.threadName] = entry;
        classify(entry);
      });
    });

    Object.keys(callStackMap).forEach(value => {
      if (callStackMap[value].level > 0) {
        usefulCallStackMap[value] = callStackMap[value];
      }
    });

    Object.keys(groupMap).forEach(value => {
      if (groupMap[value].level > 0) {
        usefulGroupMap[value] = groupMap[value];
      }
    });

    Object.keys(usefulCallStackMap).forEach((value, index) => {
      const entries: JstackEntry[] = usefulCallStackMap[value].entryList;
      const threadState = entries[0].threadState.value;
      const threadCount = entries.length;
      const level = usefulCallStackMap[value].level;
      // const words = usefulCallStackMap[value].keyword;
      const words = (
        <List
          size={'small'}
          dataSource={usefulCallStackMap[value].keyword}
          renderItem={item => this.renderItem(item)}
          locale={{emptyText: ''}}
        />
      );
      const title = (
        <Link
          to={{pathname: '/jstack/identical_trace', state: {callStack: value, entryList: entries}}}
          style={{fontSize: '18px', cursor: 'pointer', textDecoration: 'underline', color: 'inherit'}}
        >
          {threadCount} {threadState}
        </Link>
      );

      // get head ${foldLength} lines
      let i = -1;
      let n = this.foldLength;
      while (n-- && i++ < value.length) {
        i = value.indexOf('\n', i);
        if (i < 0) {
          break;
        }
      }
      let stackTrace = <p style={Style.wordWrap}>{value}</p>;
      if (i > 0) {
        stackTrace = <p style={Style.wordWrap}>{value.substring(0, i) + '\n\t...'}</p>;
      }

      traceData.push({
        key: index,
        state: threadState,
        count: threadCount,
        title: title,
        trace: stackTrace,
        level: level,
        words: words,
      });
    });

    noCallStackArray.forEach(entry => {
      total++;
      entryMap[entry.threadName] = entry;
      classify(entry);
    });

    const label = ['new', 'runnable', 'blocked', 'waiting', 'timed_waiting', 'terminated'];
    stateMap[label[0]] = newThread;
    stateMap[label[1]] = runnable;
    stateMap[label[2]] = blocked;
    stateMap[label[3]] = waiting;
    stateMap[label[4]] = timedWaiting;
    stateMap[label[5]] = terminated;

    methodList.forEach((value, index) => {
      const count = value.entryList.length;
      const title = (
        <Link
          to={{pathname: '/jstack/hot_method', state: {method: value.method, entryList: value.entryList}}}
          style={{
            fontSize: '18px',
            cursor: 'pointer',
            textDecoration: 'underline',
            color: JstackStyle.threadLink.color
          }}
        >
          {count} threads
        </Link>
      );
      methodData.push({
        key: index,
        count: count,
        title: title,
        method: <p style={Style.wordWrap}>{value.method}</p>,
      });
    });

    Object.keys(usefulGroupMap).forEach((value, index) => {
      const entries: JstackEntry[] = usefulGroupMap[value].entryList;
      const threadCount = entries.length;
      const level = usefulGroupMap[value].level;
      const title = (
        <Link
          to={{
            pathname: '/jstack/thread_group', state: {groupName: value, entryList: entries}
          }}
          style={{fontSize: '18px', cursor: 'pointer', textDecoration: 'underline', color: 'inherit'}}
        >
          {threadCount} threads
        </Link>
      );
      groupData.push({
        key: index,
        count: threadCount,
        title: title,
        groupName: <p style={Style.wordWrap}>{value}</p>,
        level: level,
      });
    });

    return {
      entryMap: entryMap,
      statistic: [total, newThread.length, runnable.length, blocked.length, waiting.length, timedWaiting.length, terminated.length],
      stateMap: stateMap,
      traceData: traceData,
      methodData: methodData,
      groupData: groupData,
    };
  }

  updateCallStack = (callStackMap: {
    [callStack: string]: JstackEntryWithLevel,
  }) => {

    const newUsefulCallStackMap = {};
    const newTraceData: any[] = [];
    Object.keys(callStackMap).forEach(value => {
      if (callStackMap[value].level > 0) {
        newUsefulCallStackMap[value] = callStackMap[value];
      }
    });

    Object.keys(newUsefulCallStackMap).forEach((value, index) => {
      const entries: JstackEntry[] = newUsefulCallStackMap[value].entryList;
      const threadState = entries[0].threadState.value;
      const threadCount = entries.length;
      const level = newUsefulCallStackMap[value].level;
      // const words = newUsefulCallStackMap[value].keyword;
      const words = (
        <List
          dataSource={newUsefulCallStackMap[value].keyword}
          renderItem={item => this.renderItem(item)}
          locale={{emptyText: ''}}
        />
      );
      const title = (
        <Link
          to={{pathname: '/jstack/identical_trace', state: {callStack: value, entryList: entries}}}
          style={{fontSize: '18px', cursor: 'pointer', textDecoration: 'underline', color: 'inherit'}}
        >
          {threadCount} {threadState}
        </Link>
      );

      // get head ${foldLength} lines
      let i = -1;
      let n = this.foldLength;
      while (n-- && i++ < value.length) {
        i = value.indexOf('\n', i);
        if (i < 0) {
          break;
        }
      }
      let stackTrace = <p style={Style.wordWrap}>{value}</p>;
      if (i > 0) {
        stackTrace = <p style={Style.wordWrap}>{value.substring(0, i) + '\n\t...'}</p>;
      }

      newTraceData.push({
        key: index,
        state: threadState,
        count: threadCount,
        title: title,
        trace: stackTrace,
        level: level,
        words: words,
      });
    });

    this.setState({traceData: newTraceData});
  }

  updateThreadGroup = (groupMap: {
    [groupName: string]: JstackEntryWithLevel,
  }) => {

    const newUsefulGroupMap = {};
    const newGroupData: any[] = [];
    Object.keys(groupMap).forEach(value => {
      if (groupMap[value].level > 0) {
        newUsefulGroupMap[value] = groupMap[value];
      }
    });

    Object.keys(newUsefulGroupMap).forEach((value, index) => {
      const entries: JstackEntry[] = newUsefulGroupMap[value].entryList;
      const threadCount = entries.length;
      const level = newUsefulGroupMap[value].level;
      const title = (
        <Link
          to={{
            pathname: '/jstack/thread_group', state: {groupName: value, entryList: entries}
          }}
          style={{fontSize: '18px', cursor: 'pointer', textDecoration: 'underline', color: 'inherit'}}
        >
          {threadCount} threads
        </Link>
      );
      newGroupData.push({
        key: index,
        count: threadCount,
        title: title,
        groupName: <p style={Style.wordWrap}>{value}</p>,
        level: level,
      });
    });
    this.setState({groupData: newGroupData});
  }

  summaryOption = () => {
    const data: any[] = [];
    const label = ['New', 'Runnable', 'Blocked', 'Waiting', 'Timed_Waiting', 'Terminated'];
    const style = [JstackStyle.blue, JstackStyle.green, JstackStyle.red, JstackStyle.yellow, JstackStyle.orange];
    this.state.statistic.forEach((value, index) => {
      if (index !== 0 && value !== 0) {
        data.push({
          name: label[index - 1],
          value: value,
          itemStyle: style[index - 1],
        });
      }
    });
    return {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
      },
      series: [{
        type: 'pie',
        name: 'Thread State',
        data: data,
        radius: ['37%', '57%'],
        label: {
          fontSize: 16,
          formatter: '{a|{a}}\n{hr|}\n {b|{b}: }{c} {per|{d}%} ',
          backgroundColor: '#eeeeee',
          borderColor: '#aaaaaa',
          borderWidth: 1,
          borderRadius: 4,
          rich: {
            a: {
              color: '#999',
              lineHeight: 22,
              align: 'center'
            },
            hr: {
              borderColor: '#aaaaaa',
              width: '100%',
              borderWidth: 0.5,
              height: 0
            },
            b: {
              fontSize: 16,
              lineHeight: 33
            },
            per: {
              color: '#eeeeee',
              backgroundColor: '#334455',
              padding: [2, 4],
              borderRadius: 2
            }
          },
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    };
  }

  onChartClick = (params) => {
    this.props.history.push({
      pathname: '/jstack/thread_status',
      state: {
        entries: this.state.stateMap[params.data.name.toLowerCase()],
      }
    });
  }

  constructor(props: any) {
    super(props);
    const response = props.fileInfo;
    const stored = window.sessionStorage
      .getItem(`jstack/dashboard-file-${props.fileInfo.fileName}-${props.locationKey}`);
    const storedData = stored ? JSON.parse(stored) : null;
    const data = this.genData(response);
    this.state = {
      timestamp: response.timeStamp,
      entryMap: data.entryMap as EntryMap,
      lockMap: response.lockMap,
      statistic: data.statistic,
      stateMap: data.stateMap,
      traceData: data.traceData,
      traceOrder: stored ? storedData.traceOrder : 'descend',
      tracePage: stored ? storedData.tracePage : 1,
      traceFilter: stored ? storedData.traceFilter : [],
      methodData: data.methodData,
      methodOrder: stored ? storedData.methodOrder : 'descend',
      methodPage: stored ? storedData.methodPage : 1,
      groupData: data.groupData,
      groupOrder: stored ? storedData.groupOrder : 'descend',
      groupPage: stored ? storedData.groupPage : 1,
    }
    ;
    Store.dispatch({
      type: 'jstack',
      entryMap: data.entryMap,
      lockMap: response.lockMap,
    });
  }

  componentWillReceiveProps(nextProps: any) {
    this.updateCallStack(nextProps.fileInfo.callStackMap);
    this.updateThreadGroup(nextProps.fileInfo.groupMap);
  }

  componentWillUnmount() {
    const data = {
      traceOrder: this.state.traceOrder,
      tracePage: this.state.tracePage,
      traceFilter: this.state.traceFilter,
      methodOrder: this.state.methodOrder,
      methodPage: this.state.methodPage,
      groupOrder: this.state.groupOrder,
      groupPage: this.state.groupPage,
    };
    window.sessionStorage
      .setItem(`jstack/dashboard-file-${this.props.fileInfo.fileName}-${this.props.locationKey}`, JSON.stringify(data));
  }

  render() {
    const traceColumns = [{
      title: 'Identical Stack Trace ',
      dataIndex: 'trace',
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }, {
      title: 'Keywords',
      dataIndex: 'words',
      width: 200,
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }, {
      title: 'Thread Count',
      dataIndex: 'title',
      width: 200,
      filters: [
        {text: 'New', value: 'NEW'},
        {text: 'Runnable', value: 'RUNNABLE'},
        {text: 'Blocked', value: 'BLOCKED'},
        {text: 'Waiting', value: 'WAITING'},
        {text: 'Timed_Waiting', value: 'TIMED_WAITING'},
        {text: 'Terminated', value: 'TERMINATED'},
      ],
      filteredValue: this.state.traceFilter,
      onFilter: (value, record) => record.state === value,
      sorter: (a, b) => {
        if (a.level > b.level) {
          if (this.state.traceOrder === 'ascend') {
            return -1;
          } else {
            return 1;
          }
        } else if (a.level < b.level) {
          if (this.state.traceOrder === 'ascend') {
            return 1;
          } else {
            return -1;
          }
        } else {
          return a.count - b.count;
        }
      },
      sortOrder: this.state.traceOrder,
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }];
    const methodColumns = [{
      title: 'Thread Count',
      width: 150,
      dataIndex: 'title',
      sorter: (a, b) => a.count - b.count,
      sortOrder: this.state.methodOrder,
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }, {
      title: 'Method',
      dataIndex: 'method',
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }];
    const groupColumns = [{
      title: 'Group Name',
      dataIndex: 'groupName',
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }, {
      title: 'Thread Count',
      dataIndex: 'title',
      width: 200,
      sorter: (a, b) => {
        if (a.level > b.level) {
          if (this.state.groupOrder === 'ascend') {
            return -1;
          } else {
            return 1;
          }
        } else if (a.level < b.level) {
          if (this.state.groupOrder === 'ascend') {
            return 1;
          } else {
            return -1;
          }
        } else {
          return a.count - b.count;
        }
      },
      sortOrder: this.state.groupOrder,
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }];

    return (
      <div>
        <div style={{...Style.block, display: 'flex'}}>
          <div style={{flex: 1}}>
            <h3 style={{fontWeight: 'bold', marginBottom: '20px'}}>Thread Summary</h3>
            <h4>Timestamp: <span style={{fontWeight: 'bold'}}>{this.state.timestamp}</span></h4>
            <h4>Total Threads Count: <span style={{fontWeight: 'bold'}}>{this.state.statistic[0]}</span></h4>
            {this.state.statistic[0] > 0 && (
              <h4 style={{textDecoration: 'underline'}}>
                <Link to="/jstack/lock_network" style={{color: 'inherit'}}>Lock Network</Link>
              </h4>
            )}
          </div>
          {this.state.statistic[0] > 0 && (
            <div style={{flex: 2, position: 'relative', marginTop: '10px'}}>
              <div
                style={{
                  fontWeight: 'bold',
                  fontSize: this.state.statistic[0] > 9999 ? '34px' : '40px',
                  color: 'rgba(58, 60, 77, 0.5)',
                  position: 'absolute',
                  display: 'flex',
                  top: 0, left: 0, bottom: 0, right: 0,
                  justifyContent: 'center',
                  alignItems: 'center',
                }}
              >
                {this.state.statistic[0]}
              </div>
              <ReactEcharts option={this.summaryOption()} onEvents={{'click': this.onChartClick}}/>
            </div>
          )}
        </div>

        {this.state.traceData !== undefined && this.state.traceData !== null && this.state.traceData.length > 0 && (
          <div style={Style.block}>
            <h3 style={{fontWeight: 'bold', marginBottom: '10px'}}>Identical Stack Trace </h3>
            <Table
              columns={traceColumns}
              dataSource={this.state.traceData}
              rowClassName={(record, index) => {
                let style = JstackStyle.stateStyle(record.state)[1];
                if (index % 2 === 1) {
                  style += ' table_row_bg';
                }
                return style;
              }}
              onChange={(pagination, filters, sorter) => this.setState({
                traceOrder: sorter.order,
                traceFilter: filters.title
              })}
              pagination={{
                showQuickJumper: true,
                onChange: (page) => this.setState({tracePage: page}),
                current: this.state.tracePage,
              }}
            />
          </div>
        )}

        {this.state.methodData !== undefined && this.state.methodData !== null && this.state.methodData.length > 0 && (
          <div style={Style.block}>
            <h3 style={{fontWeight: 'bold', marginBottom: '10px'}}>Most Used Methods</h3>
            <Table
              columns={methodColumns}
              dataSource={this.state.methodData}
              rowClassName={(record, index) => {
                return (index % 2 === 1) ? 'table_row_bg' : '';
              }}
              onChange={(pagination, filters, sorter) => this.setState({methodOrder: sorter.order})}
              pagination={{
                showQuickJumper: true,
                onChange: (page) => this.setState({methodPage: page}),
                current: this.state.methodPage,
              }}
            />
          </div>
        )}

        {this.state.groupData !== undefined && this.state.groupData !== null && this.state.groupData.length > 0 && (
          <div style={Style.block}>
            <h3 style={{fontWeight: 'bold', marginBottom: '10px'}}>Thread Group </h3>
            <Table
              columns={groupColumns}
              dataSource={this.state.groupData}
              rowClassName={(record, index) => {
                return (record.level === 2) ? 'table_row_bg' : '';
              }}
              onChange={(pagination, filters, sorter) => this.setState({groupOrder: sorter.order})}
              pagination={{
                showQuickJumper: true,
                onChange: (page) => this.setState({groupPage: page}),
                current: this.state.groupPage,
              }}
            />
          </div>
        )}
      </div>
    );
  }
}
