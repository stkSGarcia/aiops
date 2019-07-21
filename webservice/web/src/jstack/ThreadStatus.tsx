import * as React from 'react';
import * as Style from '../Style';
import EntryList from './util/EntryList';
import { BackTop } from 'antd';
import { JstackEntry } from './conf/DataStructure';

export default class ThreadStatus extends React.Component<any, any> {
  entries = this.props.location.state.entries as JstackEntry[];

  componentDidMount() {
    window.scrollTo(0, 0);
  }

  render() {
    return (
      <div>
        <BackTop/>
        <h2 style={{fontWeight: 'bold', marginBottom: '20px'}}>Thread List</h2>
        <div style={Style.block}>
          <EntryList entries={this.entries} locationKey={`jstack/thread_status-${this.props.location.key}`}/>
        </div>
      </div>
    );
  }
}
