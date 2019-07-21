import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ErrorBoundary from '../ErrorBoundary';
import Uploader from './Uploader';
import Dashboard from './Dashboard';
import ThreadStatus from './ThreadStatus';
import IdenticalTrace from './IdenticalTrace';
import HotMethod from './HotMethod';
import LockNetwork from './LockNetwork';
import Lock from './Lock';
import Thread from './Thread';
import ThreadGroup from './ThreadGroup';
// import HistoryDashboard from './HistoryDashboard';

export default class JstackRoute extends React.Component<any, any> {
  render() {
    const match = this.props.match.url;
    const home = `${match}/upload`;
    return (
      <ErrorBoundary redirect={home}>
        <Switch>
          <Redirect from={`${match}/`} to={home} exact={true}/>
          <Route path={`${match}/upload`} component={Uploader}/>
          <Route path={`${match}/dashboard`} component={Dashboard}/>
          {/*<Route path={`${match}/history_dashboard`} component={HistoryDashboard}/>*/}
          <Route path={`${match}/thread_status`} component={ThreadStatus}/>
          <Route path={`${match}/identical_trace`} component={IdenticalTrace}/>
          <Route path={`${match}/hot_method`} component={HotMethod}/>
          <Route path={`${match}/thread_group`} component={ThreadGroup}/>
          <Route path={`${match}/lock_network`} component={LockNetwork}/>
          <Route path={`${match}/lock`} component={Lock}/>
          <Route path={`${match}/thread`} component={Thread}/>
          <Redirect to={home}/>
        </Switch>
      </ErrorBoundary>
    );
  }
}
