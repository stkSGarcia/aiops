import * as React from 'react';
import * as Style from '../Style';
import EntryList from './util/EntryList';
import { BackTop } from 'antd';

export default class ThreadGroup extends React.Component<any, any> {
  data = this.props.location.state;

  componentDidMount() {
    window.scrollTo(0, 0);
  }

  render() {
    return (
      <div>
        <BackTop/>
        <h2 style={{fontWeight: 'bold', marginBottom: '20px'}}>
          {this.data.entryList.length} threads in {this.data.groupName}
        </h2>
        <div style={Style.block}>
          <EntryList entries={this.data.entryList} locationKey={`jstack/thread_group-${this.props.location.key}`}/>
        </div>
      </div>
    );
  }
}
