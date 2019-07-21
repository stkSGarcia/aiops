import * as React from 'react';
import * as Style from '../../Style';
import * as LogStyle from '../conf/LogStyle';
import * as TimelineUtil from './TimelineUtil';
import * as FilterUtil from './FilterUtil';
import * as moment from 'moment';
import Store from '../conf/CacheStore';
import Config from '../../Config';
import TimelineFilter from './TimelineFilter';
// import HourRangeSlider from '../util/HourRangeSlider';
import axios from 'axios';
import vis from 'vis/dist/vis';
import 'vis/dist/vis.css';
import { InceptorGoalResponse, InceptorTimelineResponse } from '../conf/DataStructure';
import { Button, notification, Pagination } from 'antd';

interface TimelineProps {
  date: number;
  loaded: any;
  history: any;
}

interface TimelineState {
  date: number;
  itemDataSet?: vis.DataSet;
  groupDataSet?: vis.DataSet;
  min?: number;
  max?: number;
  // sessions?: InceptorSessionResponse;
  filterFields: any;
  page: number;
  totalNum?: number;
  filterVisible: boolean;
}

export default class Timeline extends React.Component<TimelineProps, TimelineState> {
  pageSize: number = 20;
  timeline: any;
  sessionTimeline: any;

  showTimeline = (id, items, min, max, groups?) => {
    const container = document.getElementById(id);
    const specialOption = id === 'timeline' ? {
      minHeight: 600,
      stack: false,
    } : {
      height: 300,
      verticalScroll: true,
    };
    // let end = moment(min).add(1, 'hours');
    // if (moment(max).isBefore(end)) {
    //   end = moment(max);
    // }
    const option = {
      orientation: 'top',
      horizontalScroll: true,
      zoomKey: 'ctrlKey',
      min: moment(min),
      max: moment(max),
      // min: moment(this.state.date - 28800000),
      // max: moment(this.state.date + 57600000),
      // start: moment(min),
      // end: end,
      // zoomMax: 3600000,
      tooltip: {
        followMouse: true,
      },
      ...specialOption
    };
    if (groups === undefined) {
      this.sessionTimeline = new vis.Timeline(container, items, option);
    } else {
      this.timeline = new vis.Timeline(container, items, groups, option);
    }
  }

  onItemClick = (event) => {
    const props = this.timeline.getEventProperties(event);
    if (props.what !== 'item') {
      return;
    }
    const item = this.state.itemDataSet.get(props.item);
    axios.post('/api/log/inceptor/goal', {goalID: item.id, date: this.state.date}, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        this.props.history.push({
          pathname: '/log/inceptor/goal',
          state: {
            goal: (res.data as InceptorGoalResponse).goal,
            date: this.state.date,
          }
        });
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  handleFormChange = (changedFields) => {
    this.setState(({filterFields}) => ({
      filterFields: {...filterFields, ...changedFields},
    }));
  }

  handleSubmit = () => {
    this.setState(({filterFields}) => ({
      page: 1, filterFields: {...filterFields, submitting: true, resetting: false}
    }));
    const fieldData = this.state.filterFields;
    const indices = FilterUtil.indices(1, this.pageSize);
    const timeRange = FilterUtil.timeRange(this.state.date, [fieldData.time.value.from, fieldData.time.value.to]);
    const durationRange = FilterUtil.duration([fieldData.duration.value.min, fieldData.duration.value.max]);
    const bean = {
      date: this.state.date,
      startIndex: indices[0],
      endIndex: indices[1],
      startTime: timeRange[0],
      endTime: timeRange[1],
      smartType: fieldData.smart.value ? 1 : 0,
      goalType: FilterUtil.goalType(fieldData.goal.value),
      minDuration: durationRange[0],
      maxDuration: durationRange[1],
      sortBy: fieldData.sorter.value,
      order: fieldData.order.value === 'asc',
    };
    this.changeData(bean);
  }

  handleReset = (e) => {
    e.preventDefault();
    this.setState({
      page: 1,
      filterFields: {
        sorter: {value: '3'},
        order: {value: 'desc'},
        smart: {value: false},
        goal: {value: []},
        time: {value: {from: -28800000, to: -28800000}},
        duration: {value: {min: undefined, max: undefined}},
        submitting: false,
        resetting: true,
      },
    });
    const indices = FilterUtil.indices(1, this.pageSize);
    const bean = {
      date: this.state.date,
      startIndex: indices[0],
      endIndex: indices[1],
      startTime: -1,
      endTime: -1,
      smartType: 0,
      goalType: -1,
      minDuration: -1,
      maxDuration: -1,
      sortBy: '3',
      order: false,
    };
    this.changeData(bean);

  }

  onPageChange = (page) => {
    this.setState({page: page});
    const fieldData = this.state.filterFields;
    const indices = FilterUtil.indices(page, this.pageSize);
    const timeRange = FilterUtil.timeRange(this.state.date, [fieldData.time.value.from, fieldData.time.value.to]);
    const durationRange = FilterUtil.duration([fieldData.duration.value.min, fieldData.duration.value.max]);
    const bean = {
      date: this.state.date,
      startIndex: indices[0],
      endIndex: indices[1],
      startTime: timeRange[0],
      endTime: timeRange[1],
      smartType: fieldData.smart.value ? 1 : 0,
      goalType: FilterUtil.goalType(fieldData.goal.value),
      minDuration: durationRange[0],
      maxDuration: durationRange[1],
      sortBy: fieldData.sorter.value,
      order: fieldData.order.value === 'asc',
    };
    this.changeData(bean);
  }

  changeData = (bean) => {
    axios.post('/api/log/inceptor/timeline', bean, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        const response = res.data as InceptorTimelineResponse;
        const data = TimelineUtil.timelineData(response.goalsBySession, bean.startIndex);
        const dataSet = TimelineUtil.dataSet(data.items, data.groups);
        this.setState(({filterFields}) => ({
          itemDataSet: dataSet[0],
          groupDataSet: dataSet[1],
          min: data.min,
          max: data.max,
          totalNum: response.size,
          filterFields: {...filterFields, submitting: false, resetting: false},
        }));
        this.timeline.setData({
          items: dataSet[0],
          groups: dataSet[1],
        });
        this.timeline.setOptions({
          start: data.min,
          end: data.max,
          min: data.min,
          max: data.max,
        });
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
        this.setState(({filterFields}) => ({
          filterFields: {...filterFields, submitting: false, resetting: false}
        }));
      });
  }

  constructor(props: TimelineProps) {
    super(props);
    const rawData = Store.getState();
    if (rawData && rawData.timeline) {
      const timelineData = rawData.timeline;
      if (timelineData.hasOwnProperty(props.date)) {
        const data = timelineData[props.date];
        this.state = {
          date: props.date,
          itemDataSet: data.itemDataSet,
          groupDataSet: data.groupDataSet,
          min: data.min,
          max: data.max,
          // sessions: data.sessions,
          filterFields: data.filterFields,
          page: data.page,
          totalNum: data.totalNum,
          filterVisible: data.filterVisible,
        };
        return;
      }
    }
    this.state = {
      date: props.date,
      filterFields: {
        sorter: {value: '3'},
        order: {value: 'desc'},
        smart: {value: false},
        goal: {value: []},
        time: {value: {from: -28800000, to: -28800000}},
        duration: {value: {min: undefined, max: undefined}},
        submitting: false,
        resetting: false,
      },
      page: 1,
      filterVisible: false,
    };
  }

  componentDidMount() {
    if (this.state.itemDataSet !== undefined) {
      this.showTimeline('timeline', this.state.itemDataSet, this.state.min, this.state.max, this.state.groupDataSet);
      // this.showTimeline('sessionTimeline', sessionItems, this.state.sessions!.min, this.state.sessions!.max);
      this.props.loaded();
    } else {
      const fieldData = this.state.filterFields;
      const indices = FilterUtil.indices(this.state.page, this.pageSize);
      const timeRange = FilterUtil.timeRange(this.state.date, [fieldData.time.value.from, fieldData.time.value.to]);
      const bean = {
        date: this.state.date,
        startIndex: indices[0],
        endIndex: indices[1],
        startTime: timeRange[0],
        endTime: timeRange[1],
        smartType: fieldData.smart.value ? 1 : 0,
        goalType: -1,
        minDuration: undefined,
        maxDuration: undefined,
        sortBy: fieldData.sorter.value,
        order: fieldData.order.value === 'asc',
      };
      axios.post('/api/log/inceptor/timeline', bean, {timeout: Config.REQUEST_TIMEOUT})
        .then(res => {
          const response = res.data as InceptorTimelineResponse;
          const data = TimelineUtil.timelineData(response.goalsBySession, bean.startIndex);
          const dataSet = TimelineUtil.dataSet(data.items, data.groups);
          this.setState({
            itemDataSet: dataSet[0],
            groupDataSet: dataSet[1],
            min: data.min,
            max: data.max,
            totalNum: response.size,
          });
          this.showTimeline('timeline', dataSet[0], data.min, data.max, dataSet[1]);
          this.props.loaded();
        })
        .catch(err => {
          notification.error({message: 'ERROR', description: err.message, duration: 3});
          this.props.loaded();
        });
      // axios.post('/api/log/inceptor/session', {date: this.state.date}, {timeout: Config.REQUEST_TIMEOUT})
      //   .then(res => {
      //     const response = res.data as InceptorSessionResponse;
      //     const sessionItems = Timeline.sessionData(response.sessions);
      //     this.setState({sessions: response});
      //     this.showTimeline('sessionTimeline', sessionItems, response.min, response.max);
      //   })
      //   .catch(err => {
      //     notification.error({message: 'ERROR', description: err.message, duration: 3});
      //   });
    }
  }

  componentWillUnmount() {
    const data = {
      itemDataSet: this.state.itemDataSet,
      groupDataSet: this.state.groupDataSet,
      min: this.state.min,
      max: this.state.max,
      // sessions: this.state.sessions,
      filterFields: this.state.filterFields,
      page: this.state.page,
      totalNum: this.state.totalNum,
      filterVisible: this.state.filterVisible,
    };
    Store.dispatch({
      type: 'inceptor-timeline',
      date: this.state.date,
      data: data,
    });
  }

  render() {
    return (
      <div>
        <Button
          style={{float: 'right'}}
          icon="filter"
          onClick={() => this.setState({filterVisible: !this.state.filterVisible})}
        >
          Filter
        </Button>
        {/*<HourRangeSlider*/}
        {/*date={this.state.date}*/}
        {/*range={[this.state.min!, this.state.max!]}*/}
        {/*style={{width: '400px', float: 'right', marginRight: '30px'}}*/}
        {/*onChange={value => this.timeline.setWindow(moment(value[0]), moment(value[1]))}*/}
        {/*/>*/}
        <p style={{color: LogStyle.blue.color, fontSize: '16px', fontWeight: 'bold', float: 'left', margin: '10px'}}>
          {moment(this.state.date).format(Config.DATE_FORMAT)}
        </p>
        <div style={{clear: 'both'}}/>
        <div style={this.state.filterVisible ? {...Style.showDiv, ...LogStyle.filter} : Style.hideDiv}>
          <TimelineFilter
            {...this.state.filterFields}
            onChange={this.handleFormChange}
            onSubmit={this.handleSubmit}
            onReset={this.handleReset}
          />
        </div>
        <div id="timeline" style={{marginTop: '20px'}} onClick={this.onItemClick}/>
        <Pagination
          style={{float: 'right', marginTop: '20px'}}
          total={this.state.totalNum}
          current={this.state.page}
          pageSize={this.pageSize}
          onChange={this.onPageChange}
          showQuickJumper={true}
        />
        <div style={{clear: 'both'}}/>
        <div id="sessionTimeline" style={{marginTop: '20px'}}/>
      </div>
    );
  }
}
