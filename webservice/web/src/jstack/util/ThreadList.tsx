import * as React from 'react';
import * as Style from '../../Style';
import * as JstackStyle from '../conf/JstackStyle';
import Store from '../conf/CacheStore';
import { JstackEntry } from '../conf/DataStructure';
import { List } from 'antd';
import { Link } from 'react-router-dom';

interface ThreadListProps {
  entries: JstackEntry[];
  title?: string;
  columns?: number;
}

interface ThreadListState {
  columns: number;
}

export default class ThreadList extends React.Component<ThreadListProps, ThreadListState> {
  entryList = () => {
    const entries = this.props.entries;
    const columns = this.state.columns;
    const storedData = Store.getState();
    const entryMap = storedData.entryMap;

    const baseSize = Math.floor(entries.length / columns);
    const restSize = entries.length - columns * baseSize;
    const groupSize = baseSize === 0 ? restSize : columns;
    let i = 0;
    let j = 0;
    const groups: JstackEntry[][] = [];
    while (i < groupSize) {
      if (i < restSize) {
        groups.push(entries.slice(j, j + baseSize + 1));
        j += baseSize + 1;
      } else {
        groups.push(entries.slice(j, j + baseSize));
        j += baseSize;
      }
      i++;
    }

    const renderItem = (item: JstackEntry, index: number) => (
      <List.Item key={index} style={{...Style.rowBackground(index), borderRadius: '6px'}}>
        <Link
          to={{pathname: '/jstack/thread', state: {entry: entryMap[item.threadName]}}}
          style={JstackStyle.threadLink}
        >
          {item.threadName}
        </Link>
      </List.Item>
    );

    return groups.map((value, index) => (
      <div style={{flex: 1, margin: '0 10px'}} key={index}>
        <List dataSource={value} renderItem={renderItem}/>
      </div>
    ));
  }

  constructor(props: ThreadListProps) {
    super(props);
    this.state = {
      columns: this.props.columns === undefined ? 3 : this.props.columns,
    };
  }

  render() {
    return (
      <div>
        {this.props.title !== undefined &&
        <h4 style={{...Style.headCell, padding: '10px', borderRadius: '5px 5px 0 0'}}>{this.props.title}</h4>
        }
        <div style={{display: 'flex', margin: '0 -10px'}}>
          {this.entryList()}
        </div>
      </div>
    );
  }
}
