import * as React from 'react';
import * as moment from 'moment';
import * as Common from './util/CommonHTML';
import axios from 'axios';
import Config from '../Config';
import IntervalStatistics from './IntervalStatistics';
import { notification, Tabs } from 'antd';
import { StatisticsResponse } from './conf/DataStructure';

const TabPane = Tabs.TabPane;

interface DashboardState {
  tab: string;
  statsData?: {
    Today: {
      time: [number, number],
      data?: StatisticsResponse,
    },
    Month: {
      time: [number, number],
      data?: StatisticsResponse,
    },
    History: {
      time: [number, number],
      data?: StatisticsResponse,
    },
  };
}

export default class Dashboard extends React.Component<any, DashboardState> {
  tabs = () => {
    const filteredData = Object.keys(this.state.statsData!)
      .filter(value => {
        const data = this.state.statsData![value].data;
        return data && (data.totalDocs !== 0
          || data.totalQueries !== 0
          || data.topAnswers.length !== 0);
      });
    if (filteredData.length === 0) {
      return Common.noDataHTML;
    }
    return (
      <Tabs activeKey={this.state.tab} onChange={(key) => this.setState({tab: key})}>
        {filteredData.map((value, index) => {
            const data = this.state.statsData![value];
            return (
              <TabPane tab={value} key={`${index}`}>
                <IntervalStatistics
                  startTime={data.time[0]}
                  endTime={data.time[1]}
                  stats={data.data}
                  history={this.props.history}
                />
              </TabPane>
            );
          }
        )}
      </Tabs>
    );
  }

  intervalData = async (startTime: number, endTime: number) => {
    const data = {
      startTime: startTime,
      endTime: endTime,
    };
    try {
      const res = await axios.get(`${Config.API_VERSION}/docs/statistics`, {params: data});
      const resultHead = res.data.head;
      if (resultHead.resultCode === Config.RES_SUCCESS) {
        return res.data.data;
      } else {
        notification.error({message: 'ERROR', description: resultHead.message, duration: 3});
        return undefined;
      }
    } catch (err) {
      notification.error({message: 'ERROR', description: err.message, duration: 3});
      return undefined;
    }
  }

  constructor(props: any) {
    super(props);
    const stored = window.sessionStorage.getItem(`dashboard-${props.location.key}`);
    const storedData = stored ? JSON.parse(stored) : null;
    this.state = {
      tab: stored ? storedData.tab : '0',
    };
  }

  componentDidMount() {
    axios.get(`${Config.API_VERSION}/util/time`)
      .then(async res => {
        const head = res.data.head;
        if (head.resultCode === Config.RES_SUCCESS) {
          const currentTime = res.data.data.timestamp;
          const todayStart = moment(currentTime).startOf('day').valueOf();
          const monthStart = moment(todayStart).subtract(1, 'months').valueOf();
          const today = await this.intervalData(todayStart, currentTime);
          const month = await this.intervalData(monthStart, todayStart);
          const history = await this.intervalData(-1, -1);
          this.setState({
            statsData: {
              Today: {
                time: [todayStart, currentTime],
                data: today,
              },
              Month: {
                time: [monthStart, todayStart],
                data: month,
              },
              History: {
                time: [-1, -1],
                data: history,
              },
            },
          });
        } else {
          notification.error({message: 'ERROR', description: head.message, duration: 3});
        }
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  componentWillUnmount() {
    const data = {
      tab: this.state.tab,
    };
    window.sessionStorage.setItem(`dashboard-${this.props.location.key}`, JSON.stringify(data));
  }

  render() {
    return (
      <div>
        <h2 style={{fontWeight: 'bold', marginBottom: '20px'}}>Dashboard</h2>
        {this.state.statsData ? this.tabs() : Common.loadingHTML}
      </div>
    );
  }
}
