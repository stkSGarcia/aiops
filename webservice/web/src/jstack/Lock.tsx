import * as React from 'react';
import * as Style from '../Style';
import Store from './conf/CacheStore';
import ThreadList from './util/ThreadList';
import { BackTop } from 'antd';
import { JstackEntry } from './conf/DataStructure';

interface LockState {
  lockAddress: string;
}

export default class Lock extends React.Component<any, LockState> {
  constructor(props: any) {
    super(props);
    this.state = {
      lockAddress: props.location.state.lockAddress,
    };
  }

  componentDidMount() {
    window.scroll(0, 0);
  }

  componentWillReceiveProps(nextProps: any) {
    this.setState({
      lockAddress: nextProps.location.state.lockAddress,
    });
  }

  render() {
    const lockMap = Store.getState().lockMap;
    const entryList = lockMap[this.state.lockAddress] as JstackEntry[][];
    return (
      <div>
        <BackTop/>
        <h2 style={{fontWeight: 'bold'}}>Lock: {this.state.lockAddress}</h2>
        {entryList[0] !== null && entryList[0].length !== 0 && (
          <div style={Style.block}>
            <h3 style={{fontWeight: 'bold'}}>Held by</h3>
            <hr/>
            <ThreadList entries={entryList[0]}/>
          </div>
        )}
        {entryList[1] !== null && entryList[1].length !== 0 && (
          <div style={Style.block}>
            <h3 style={{fontWeight: 'bold'}}>Threads waiting for</h3>
            <hr/>
            <ThreadList entries={entryList[1]}/>
          </div>
        )}
      </div>
    );
  }
}
