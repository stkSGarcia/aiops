import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ErrorBoundary from '../ErrorBoundary';
import Dashboard from './Dashboard';
import Docs from './Docs';

export default class DashboardRoute extends React.Component<any, any> {
  render() {
    const match = this.props.match.url;
    const home = `${match}/`;
    return (
      <ErrorBoundary redirect={home}>
        <Switch>
          <Route path={home} component={Dashboard} exact={true}/>
          <Route path={`${match}/docs`} component={Docs}/>
          <Redirect to={home}/>
        </Switch>
      </ErrorBoundary>
    );
  }
}
