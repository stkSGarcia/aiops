import * as React from 'react';
import * as LogStyle from '../conf/LogStyle';
import * as moment from 'moment';
import Store from '../conf/CacheStore';
import Config from '../../Config';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { notification } from 'antd';
import { InceptorDateResponse } from '../conf/DataStructure';

interface InceptorCardState {
  dateData?: InceptorDateResponse;
}

export default class InceptorCard extends React.Component<any, InceptorCardState> {
  dateCard = () => {
    return this.state.dateData!.dates.map((dateBean, index) => (
      <Link
        to={{pathname: '/log/inceptor/view', state: {date: dateBean.date}}}
        key={index}
        style={{
          display: 'inline-block',
          width: '180px',
          height: '100px',
          margin: '10px',
          padding: '10px',
          background: '#ffffff',
          border: 'solid 1px #677bff',
          borderRadius: '10px',
          cursor: 'pointer',
        }}
      >
        <p style={{fontSize: '16px', fontWeight: 'bold', margin: 0}}>{moment(dateBean.date).format(Config.DATE_FORMAT)}</p>
        <p style={{fontSize: '12px', color: LogStyle.red.color, margin: 0}}>Error SQL: {dateBean.errorNum}</p>
        <p style={{fontSize: '12px', color: LogStyle.yellow.color, margin: 0}}>Long Duration SQL: {dateBean.longDurationNum}</p>
        <p style={{fontSize: '12px', color: LogStyle.green.color, margin: 0}}>Normal SQL: {dateBean.normalNum}</p>
      </Link>
    ));
  }

  constructor(props: any) {
    super(props);
    const rawData = Store.getState();
    if (rawData && rawData.date) {
      const data = rawData.date;
      this.state = {
        dateData: data.dateData,
      };
    } else {
      this.state = {};
    }
  }

  componentDidMount() {
    if (this.state.dateData === undefined) {
      axios.post('/api/log/inceptor/date', null, {timeout: Config.REQUEST_TIMEOUT})
        .then(res => {
          this.setState({dateData: res.data as InceptorDateResponse});
        })
        .catch(err => {
          notification.error({message: 'ERROR', description: err.message, duration: 3});
        });
    }
  }

  componentWillUnmount() {
    const data = {
      dateData: this.state.dateData,
    };
    Store.dispatch({
      type: 'inceptor-date',
      data: data,
    });
  }

  render() {
    if (this.state.dateData === undefined) {
      return <div/>;
    }
    return <div>{this.dateCard()}</div>;
  }
}
