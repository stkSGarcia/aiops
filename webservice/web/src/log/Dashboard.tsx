import * as React from 'react';
import * as LogStyle from './conf/LogStyle';
import InceptorCard from './inceptor/InceptorCard';
import { Tabs } from 'antd';

const TabPane = Tabs.TabPane;

interface DashboardState {
  activeTab: string;
}

export default class Dashboard extends React.Component<any, DashboardState> {
  componentTabs = () => (
    <Tabs defaultActiveKey={this.state.activeTab} tabPosition="top" onChange={key => this.setState({activeTab: key})}>
      {this.props.location.state.compSet.map((component, index) => (
        <TabPane tab={component.toLowerCase().replace(/( |^)[a-z]/g, L => L.toUpperCase())} key={index}>
          {this.component(component)}
        </TabPane>
      ))}
    </Tabs>
  )

  component = (component: string) => {
    switch (component) {
      case 'inceptor':
        return <InceptorCard/>;
      default:
        return <div>Unknown Component</div>;
    }
  }

  constructor(props: any) {
    super(props);
    const rawData = window.sessionStorage.getItem(`log/dashboard-${props.location.key}`);
    if (rawData) {
      const data = JSON.parse(rawData);
      this.state = {
        activeTab: data.activeTab,
      };
    } else {
      this.state = {
        activeTab: '0',
      };
    }
  }

  componentWillUnmount() {
    const data = {
      activeTab: this.state.activeTab,
    };
    window.sessionStorage.setItem(`log/dashboard-${this.props.location.key}`, JSON.stringify(data));
  }

  render() {
    return (
      <div style={LogStyle.page}>
        <h1>Log 分析</h1>
        {this.componentTabs()}
      </div>
    );
  }
}
