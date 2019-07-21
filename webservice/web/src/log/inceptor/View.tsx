import * as React from 'react';
import * as LogStyle from '../conf/LogStyle';
import Store from '../conf/CacheStore';
import Timeline from './Timeline';
import FlatList from './FlatList';
import { Button } from 'antd';

interface ViewState {
  date: number;
  activeView: string;
  changing: boolean;
}

export default class View extends React.Component<any, ViewState> {
  constructor(props: any) {
    super(props);
    const rawData = Store.getState();
    if (rawData && rawData.view) {
      const data = rawData.view;
      this.state = {
        date: props.location.state.date,
        activeView: data.activeView,
        changing: true,
      };
    } else {
      this.state = {
        date: props.location.state.date,
        activeView: 'timeline',
        changing: true,
      };
    }
  }

  componentWillUnmount() {
    const data = {
      activeView: this.state.activeView,
    };
    Store.dispatch({
      type: 'inceptor-view',
      data: data,
    });
  }

  render() {
    return (
      <div style={LogStyle.page}>
        <Button
          type="primary"
          style={{float: 'right', marginLeft: '20px'}}
          loading={this.state.changing}
          onClick={() => {
            this.setState({
              activeView: this.state.activeView === 'list' ? 'timeline' : 'list',
              changing: true,
            });
          }}
        >
          {this.state.changing ? 'Loading' : 'Change View'}
        </Button>
        {this.state.activeView === 'list' ?
          <FlatList date={this.state.date} loaded={() => this.setState({changing: false})} history={this.props.history}/> :
          <Timeline date={this.state.date} loaded={() => this.setState({changing: false})} history={this.props.history}/>}
      </div>
    );
  }
}
