import * as React from 'react';
import * as Style from '../Style';
import * as DashboardStyle from './conf/DashboardStyle';
import * as moment from 'moment';
import Config from '../Config';
import ReactEcharts from 'echarts-for-react';
import DocList from './util/DocList';
import { Table } from 'antd';
import { StatisticsResponse } from './conf/DataStructure';

interface IntervalStatisticsProps {
  startTime: number;
  endTime: number;
  stats: StatisticsResponse;
  history: any;
}

export default class IntervalStatistics extends React.Component<IntervalStatisticsProps, any> {
  pieChartOption = () => {
    const statistics = this.props.stats.compStats;
    const data: any[] = [];
    Object.keys(statistics).forEach(key => {
      data.push({
        name: key,
        value: statistics[key],
      });
    });
    return DashboardStyle.pieCharOption(data);
  }

  onPieChartClick = (params) => {
    this.props.history.push({
      pathname: '/dashboard/docs',
      state: {
        startTime: this.props.startTime,
        endTime: this.props.endTime,
        component: params.data.name,
      },
    });
  }

  personList = () => {
    const columns: any[] = [];
    const countData = {};
    this.props.stats.personStats.forEach((value, index) => {
      columns.push({
        title: value.name,
        dataIndex: `col-${index}`,
        onCell: () => {
          return {style: {backgroundColor: '#f8f8ff'}};
        },
        onHeaderCell: () => {
          return {style: Style.headCell};
        },
      });
      countData[`col-${index}`] = value.count + (value.count === 1 ? ' doc' : ' docs');
    });
    return (
      <Table
        columns={columns}
        dataSource={[countData]}
        bordered={true}
        pagination={false}
        style={{width: '90%', margin: '0 auto'}}
      />
    );
  }

  lineChartOption = () => {
    const legend: string[] = [];
    const data: any[] = [];
    this.props.stats.intervalQueries.forEach(value => {
      legend.push(moment(value.timestamp).format(Config.DATE_TIME_FORMAT));
      data.push(value.count);
    });
    return DashboardStyle.lineChartOption(legend, data);
  }

  constructor(props: IntervalStatisticsProps) {
    super(props);
  }

  render() {
    return (
      <div>
        {this.props.stats.totalDocs !== 0 && (
          <div style={Style.block}>
            <h2 style={{fontWeight: 'bold', marginBottom: '10px'}}>Document</h2>
            <h3 style={{marginLeft: '40px'}}>Group by component:</h3>
            <div style={{position: 'relative'}}>
              <div style={DashboardStyle.pieNumber(this.props.stats.totalDocs)}>{this.props.stats.totalDocs}</div>
              <ReactEcharts
                option={this.pieChartOption()}
                onEvents={{'click': this.onPieChartClick}}
              />
            </div>
            <h3 style={{marginLeft: '40px'}}>Top 10 Inputer:</h3>
            {this.personList()}
          </div>
        )}
        {this.props.stats.totalQueries !== 0 && (
          <div style={Style.block}>
            <h2 style={{fontWeight: 'bold', marginBottom: '10px'}}>Queries</h2>
            <h3 style={{marginLeft: '40px'}}>
              Total query times: {this.props.stats.totalQueries}
            </h3>
            <ReactEcharts option={this.lineChartOption()}/>
          </div>
        )}
        {this.props.stats.topAnswers.length !== 0 && (
          <div style={Style.block}>
            <h2 style={{fontWeight: 'bold', marginBottom: '10px'}}>Top Answers</h2>
            <DocList data={this.props.stats.topAnswers}/>
          </div>
        )}
      </div>
    );
  }
}
