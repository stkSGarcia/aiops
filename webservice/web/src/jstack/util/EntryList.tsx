import * as React from 'react';
import * as Style from '../../Style';
import * as JstackStyle from '../conf/JstackStyle';
import TraceHighlighter from './TraceHighlighter';
import { JstackEntry, Lock } from '../conf/DataStructure';
import { List } from 'antd';
import { Link } from 'react-router-dom';

interface EntryListProps {
  entries: JstackEntry[];
  locationKey: string;
}

interface EntryListState {
  page: number;
}

export default class EntryList extends React.Component<EntryListProps, EntryListState> {
  lockList = (locks: Lock[]) => {
    return locks.map((value, id) => (
      <Link
        key={id}
        to={{pathname: '/jstack/lock', state: {lockAddress: value.addr}}}
        style={JstackStyle.lockLink}
      >
        {'<' + value.addr + '>'}
      </Link>));
  }

  renderItem = (item: JstackEntry, index: number) => (
    <List.Item
      key={index}
      style={{
        ...Style.rowBackground(index),
        paddingLeft: '10px',
        paddingRight: '10px',
        borderRadius: '10px',
      }}
    >
      <div>
        <h3 style={{fontWeight: 'bold'}}>{item.threadName}</h3>
        {item.waitLocks !== null && item.waitLocks.length !== 0 && (
          <div style={{marginBottom: '5px'}}>
            <span style={{fontWeight: 'bold'}}>Locks Waiting: </span>
            {this.lockList(item.waitLocks)}
          </div>
        )}
        {item.locks !== null && item.locks.length !== 0 && (
          <div style={{marginBottom: '5px'}}>
            <span style={{fontWeight: 'bold'}}>Locks Held: </span>
            {this.lockList(item.locks)}
          </div>
        )}
        <p style={{margin: 0}}>{item.startLine}</p>
        {item.callStack !== null && TraceHighlighter(item.callStack)}
      </div>
    </List.Item>
  )

  constructor(props: EntryListProps) {
    super(props);
    const stored = window.sessionStorage.getItem(this.props.locationKey);
    if (stored) {
      this.state = {page: JSON.parse(stored).page};
    } else {
      this.state = {page: 1};
    }
  }

  componentWillUnmount() {
    const data = {page: this.state.page};
    window.sessionStorage.setItem(this.props.locationKey, JSON.stringify(data));
  }

  render() {
    return (
      <List
        dataSource={this.props.entries}
        renderItem={this.renderItem}
        pagination={{
          showQuickJumper: true,
          current: this.state.page,
          pageSize: 30,
          total: this.props.entries.length,
          onChange: (page) => {
            window.scrollTo(0, 0);
            this.setState({page: page});
          }
        }}
      />
    );
  }
}
