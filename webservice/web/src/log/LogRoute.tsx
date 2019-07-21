import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ErrorBoundary from '../ErrorBoundary';
import Uploader from './Uploader';
import Dashboard from './Dashboard';
import InceptorRoute from './inceptor/InceptorRoute';

export default class LogRoute extends React.Component<any, any> {
  render() {
    const match = this.props.match.url;
    const home = `${match}/upload`;
    return (
      <ErrorBoundary redirect={home}>
        <Switch>
          <Redirect from={`${match}/`} to={home} exact={true}/>
          <Route path={`${match}/upload`} component={Uploader}/>
          <Route path={`${match}/dashboard`} component={Dashboard}/>
          <Route path={`${match}/inceptor`} component={InceptorRoute}/>
          <Redirect to={home}/>
        </Switch>
      </ErrorBoundary>
    );
  }
}
