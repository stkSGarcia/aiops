import * as React from 'react';
import * as ReactDOM from 'react-dom';
import 'antd/dist/antd.css';
import registerServiceWorker from './registerServiceWorker';
import { BrowserRouter, Link, Redirect, Route, Switch } from 'react-router-dom';
import { Icon, Layout, Menu } from 'antd';
import MinesRoute from './mines/MinesRoute';
import LogRoute from './log/LogRoute';
import JstackRoute from './jstack/JstackRoute';
import DashboardRoute from './dashboard/DashboardRoute';
import SarRoute from './sar/SarRoute';
import ErrorBoundary from './ErrorBoundary';

const {Content, Footer, Sider} = Layout;
const home = '/sar';
// let refresh = true;
// const getRefreshState = () => {
//   if (refresh) {
//     refresh = false;
//     return true;
//   } else {
//     return false;
//   }
//   return  flag && (( flag = false) || true);
// };

class App extends React.Component<any, any> {
  render() {
    return (
      <Layout style={{minHeight: '100vh', fontFamily: 'Segoe UI, Tahoma, Geneva, Verdana, sans-serif'}}>
        <Sider collapsible={true}>
          <img src={'/logo.png'} style={{width: '88%', margin: '6%'}}/>
          <Menu theme="dark" mode="inline">
            {/*<Menu.Item key="search">*/}
            {/*<Icon type="search"/>*/}
            {/*<span>问题查询</span>*/}
            {/*<Link to="/mines/search"/>*/}
            {/*</Menu.Item>*/}
            {/*<Menu.Item key="update">*/}
            {/*<Icon type="plus-square"/>*/}
            {/*<span>问题录入</span>*/}
            {/*<Link to="/mines/create"/>*/}
            {/*</Menu.Item>*/}
            <Menu.Item key="sar">
              <Icon type="scan"/>
              <span>Sar 分析</span>
              <Link to="/sar"/>
            </Menu.Item>
            <Menu.Item key="log">
              <Icon type="area-chart"/>
              <span>Log 分析</span>
              <Link to="/log/upload"/>
            </Menu.Item>
            <Menu.Item key="jstack">
              <Icon type="pie-chart"/>
              <span>Jstack 分析</span>
              <Link to="/jstack/upload"/>
            </Menu.Item>
            {/*<Menu.Item key="dashboard">*/}
            {/*<Icon type="dashboard"/>*/}
            {/*<span>仪表盘</span>*/}
            {/*<Link to="/dashboard"/>*/}
            {/*</Menu.Item>*/}
            {/*<Menu.SubMenu*/}
            {/*key="advanced"*/}
            {/*title={<span><Icon type="bars"/><span>高级</span></span>}*/}
            {/*>*/}
            {/*<Menu.Item key="refactor">*/}
            {/*<Icon type="reload"/>*/}
            {/*<span>修改</span>*/}
            {/*<Link to="/mines/refactor"/>*/}
            {/*</Menu.Item>*/}
            {/*</Menu.SubMenu>*/}
          </Menu>
        </Sider>
        <Layout>
          <Content style={{padding: '30px 40px'}}>
            <Switch>
              {/*{getRefreshState() && <Redirect to={'/'}/>}*/}
              <Redirect from="/" to={home} exact={true}/>
              <Route path="/mines" component={MinesRoute}/>
              <Route path="/sar" component={SarRoute}/>
              <Route path="/log" component={LogRoute}/>
              <Route path="/jstack" component={JstackRoute}/>
              <Route path="/dashboard" component={DashboardRoute}/>
              <Redirect to={home}/>
            </Switch>
          </Content>
          <Footer style={{textAlign: 'center'}}>
            AIOps ©2018 Created by Transwarp
          </Footer>
        </Layout>
      </Layout>
    );
  }
}

ReactDOM.render(<BrowserRouter><ErrorBoundary redirect={home}><App/></ErrorBoundary></BrowserRouter>, document.getElementById('root'));
registerServiceWorker();
