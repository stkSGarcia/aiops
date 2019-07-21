import * as React from 'react';
import * as Style from '../Style';
import * as JstackStyle from './conf/JstackStyle';
import Store from './conf/CacheStore';
import ExpandableDiv from '../util/ExpandableDiv';
import TraceHighlighter from './util/TraceHighlighter';
import { BackTop, Table } from 'antd';
import { EntryMap, JstackEntry } from './conf/DataStructure';
import { Link } from 'react-router-dom';

interface IdenticalTraceState {
  callStack: string;
  entryList: JstackEntry[];
  tableData: any[];
}

export default class IdenticalTrace extends React.Component<any, IdenticalTraceState> {
  tableData = (data: JstackEntry[], entryMap: EntryMap) => {
    return data.map((entry, index) => {
      const thread = (
        <Link
          to={{pathname: '/jstack/thread', state: {entry: entryMap[entry.threadName]}}}
          style={JstackStyle.threadLink}
        >
          {entry.threadName}
        </Link>
      );
      const locks = (
        <div>
          {entry.locks !== null && entry.locks.map((value, id) => (
            <Link
              key={id}
              to={{pathname: '/jstack/lock', state: {lockAddress: value.addr}}}
              style={JstackStyle.lockLink}
            >
              {value.addr}
            </Link>
          ))}
        </div>
      );
      const waitLocks = (
        <div>
          {entry.waitLocks !== null && entry.waitLocks.map((value, id) => (
            <Link
              key={id}
              to={{pathname: '/jstack/lock', state: {lockAddress: value.addr}}}
              style={JstackStyle.lockLink}
            >
              {value.addr}
            </Link>
          ))}
        </div>
      );
      return {
        key: index,
        name: thread,
        locks: locks,
        waitLocks: waitLocks,
      };
    });
  }

  constructor(props: any) {
    super(props);
    const storedData = Store.getState();
    const data = this.tableData(props.location.state.entryList, storedData.entryMap);
    this.state = {
      callStack: props.location.state.callStack,
      entryList: props.location.state.entryList,
      tableData: data,
    };
  }

  componentDidMount() {
    window.scrollTo(0, 0);
  }

  render() {
    const columns = [{
      title: 'Threads',
      dataIndex: 'name',
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }, {
      title: 'Locks Waiting',
      dataIndex: 'waitLocks',
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }, {
      title: 'Locks Held',
      dataIndex: 'locks',
      onHeaderCell: () => {
        return {style: Style.headCell};
      },
    }];
    return (
      <div>
        <BackTop/>
        <div style={Style.block}>
          <h2 style={{fontWeight: 'bold'}}>Identical Stack Trace</h2>
          <ExpandableDiv maxHeight={250}>{TraceHighlighter(this.state.callStack)}</ExpandableDiv>
        </div>
        <div style={Style.block}>
          <h2 style={{fontWeight: 'bold', marginTop: '20px'}}>Threads with Same Stack Trace</h2>
          <Table
            columns={columns}
            dataSource={this.state.tableData}
            rowClassName={(record, index) => {
              if (index % 2 === 1) {
                return 'table_row_bg';
              } else {
                return '';
              }
            }}
            pagination={false}
          />
        </div>
      </div>
    );
  }
}
