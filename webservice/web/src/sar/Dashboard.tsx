import * as React from 'react';
import { Tabs } from 'antd';
import Store from './conf/CacheStore';
import FileDashboard from './FileDashboard';
import { SarReportBean } from './conf/DataStructure';

const TabPane = Tabs.TabPane;

interface DashboardState {
  sarReports: SarReportBean[];
  tab: string;
}

export default class Dashboard extends React.Component<any, DashboardState> {

  constructor(props: any) {
    super(props);
    const stored = window.sessionStorage.getItem(`sar/dashboard-${props.location.key}`);
    const storedData = stored ? JSON.parse(stored) : null;
    this.state = {
      sarReports: Store.getState().sarReports,
      tab: stored ? storedData.tab : '1',
    };
  }

  componentWillUnmount() {
    const data = {
      tab: this.state.tab,
    };
    window.sessionStorage.setItem(`sar/dashboard-${this.props.location.key}`, JSON.stringify(data));
  }

  render() {
    return (
      <div>
        <h2 style={{marginBottom: '20px', fontWeight: 'bold'}}>Sar 分析 </h2>

        <Tabs activeKey={this.state.tab} onChange={(key) => this.setState({tab: key})}>
          {this.state.sarReports.map((value, index) => (
            <TabPane tab={value.fileName} key={`${index + 1}`}>
              <FileDashboard
                sarReport={value}
                sarReportIndex={index}
                history={this.props.history}
                locationKey={this.props.location.key}
              />
            </TabPane>
          ))}
        </Tabs>
      </div>
    );
  }
}
