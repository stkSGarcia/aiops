import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ErrorBoundary from '../ErrorBoundary';
import Sar from './Sar';
import Dashboard from './Dashboard';

export default class SarRoute extends React.Component<any, any> {
  render() {
    const match = this.props.match.url;
    const home = `${match}/upload`;
    return (
      <ErrorBoundary redirect={home}>
        <Switch>
          <Redirect from={`${match}/`} to={home} exact={true}/>
          <Route path={`${match}/upload`} component={Sar}/>
          <Route path={`${match}/dashboard`} component={Dashboard}/>
          <Redirect to={home}/>
        </Switch>
      </ErrorBoundary>
    );
  }
}
