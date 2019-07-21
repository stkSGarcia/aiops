import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import ErrorBoundary from '../ErrorBoundary';
import Search from './Search';
import Update from './Update';

export default class LogRoute extends React.Component<any, any> {
  render() {
    const match = this.props.match.url;
    const home = `${match}/search`;
    return (
      <ErrorBoundary redirect={home}>
        <Switch>
          <Redirect from={`${match}/`} to={home} exact={true}/>
          <Route path={`${match}/search`} component={Search}/>
          <Route path={`${match}/refactor`} render={(routeProps) => <Search {...routeProps} enableEdit={true}/>}/>
          <Route path={`${match}/create`} render={(routeProps) => <Update {...routeProps} enableCreate={true}/>}/>
          <Route path={`${match}/update`} component={Update}/>
          <Redirect to={home}/>
        </Switch>
      </ErrorBoundary>
    );
  }
}
