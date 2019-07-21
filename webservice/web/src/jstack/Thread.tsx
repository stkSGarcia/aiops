import * as React from 'react';
import * as Style from '../Style';
import * as JstackStyle from './conf/JstackStyle';
import Store from './conf/CacheStore';
import TraceHighlighter from './util/TraceHighlighter';
import { JstackEntry, Lock } from './conf/DataStructure';
import { Link } from 'react-router-dom';

interface ThreadState {
  entry: JstackEntry;
}

export default class Thread extends React.Component<any, ThreadState> {
  storedData = Store.getState();
  entryMap = this.storedData.entryMap;
  lockMap = this.storedData.lockMap;

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

  constructor(props: any) {
    super(props);
    this.state = {
      entry: props.location.state.entry,
    };
  }

  componentDidMount() {
    window.scroll(0, 0);
  }

  componentWillReceiveProps(nextProps: any) {
    this.setState({
      entry: nextProps.location.state.entry,
    });
    window.scroll(0, 0);
  }

  render() {
    const entry = this.state.entry;
    return (
      <div>
        <h2 style={{fontWeight: 'bold', marginBottom: '20px'}}>Thread Summary</h2>
        <div style={Style.block}>
          <div style={{display: 'flex'}}>
            <div style={{flex: 1, marginRight: '10px'}}>
              <p><span style={{fontWeight: 'bold'}}>Thread name: </span>{entry.threadName}</p>
              <p>
                <span style={{fontWeight: 'bold'}}>Thread state: </span>
                <span style={{color: JstackStyle.stateStyle(entry.threadState.value)[0]}}>{entry.threadState.value}</span>
              </p>
              <p><span style={{fontWeight: 'bold'}}>Daemon: </span>{entry.isDaemon + ''}</p>
              <p><span style={{fontWeight: 'bold'}}>ID: </span>{entry.id || 'N/A'}</p>
            </div>
            <div style={{flex: 1, marginLeft: '10px'}}>
              <p><span style={{fontWeight: 'bold'}}>Priority: </span>{entry.prio || 'N/A'}</p>
              <p><span style={{fontWeight: 'bold'}}>OS Priority: </span>{entry.os_prio || 'N/A'}</p>
              <p><span style={{fontWeight: 'bold'}}>Native Thread ID: </span>{entry.nid || 'N/A'}</p>
              <p><span style={{fontWeight: 'bold'}}>Thread ID: </span>{entry.tid || 'N/A'}</p>
            </div>
          </div>
          {entry.waitLocks !== null && entry.waitLocks.length !== 0 && (
            <div style={{marginBottom: '10px'}}>
              <span style={{fontWeight: 'bold'}}>Locks Waiting: </span>
              {this.lockList(entry.waitLocks)}
            </div>
          )}
          {entry.locks !== null && entry.locks.length !== 0 && (
            <div style={{marginBottom: '10px'}}>
              <span style={{fontWeight: 'bold'}}>Locks Held: </span>
              {this.lockList(entry.locks)}
            </div>
          )}
          <hr/>
          {TraceHighlighter(entry.callStack)}
        </div>
      </div>
    );
  }
}
