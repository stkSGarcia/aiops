import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ErrorBoundary from '../../ErrorBoundary';
import View from './View';
import Goal from './Goal';
import Task from './Task';
import Logs from './Logs';

export default class InceptorRoute extends React.Component<any, any> {
  render() {
    const match = this.props.match.url;
    const home = `${match}/view`;
    return (
      <ErrorBoundary redirect={home}>
        <Switch>
          <Redirect from={`${match}/`} to={home} exact={true}/>
          <Route path={`${match}/view`} component={View}/>
          <Route path={`${match}/goal`} component={Goal}/>
          <Route path={`${match}/task`} component={Task}/>
          <Route path={`${match}/logs`} component={Logs}/>
          <Redirect to={home}/>
        </Switch>
      </ErrorBoundary>
    );
  }
}
