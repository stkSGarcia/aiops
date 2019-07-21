import * as React from 'react';
import * as Style from '../Style';
import * as moment from 'moment';
import * as Common from './util/CommonHTML';
import Config from '../Config';
import axios from 'axios';
import { DocItem } from '../mines/conf/DataStructure';
import { List, notification, Pagination } from 'antd';

interface DocsState {
  docs?: DocItem[];
  page: number;
  totalNum?: number;
}

export default class Docs extends React.Component<any, DocsState> {
  pageSize: number = 20;

  renderDocItem = (item: DocItem, index: number) => (
    <List.Item
      key={index}
      style={{padding: '10px', ...Style.rowBackground(index), borderRadius: '10px'}}
    >
      <div>
        <p><span style={{fontWeight: 'bold', fontSize: '16px'}}>Component:</span> {item.component}</p>
        <p style={{fontWeight: 'bold', fontSize: '16px', marginTop: '10px'}}>Solution:</p>
        <p style={Style.wordWrap}>{item.solution}</p>
        <p style={{fontWeight: 'bold', fontSize: '16px', marginTop: '30px'}}>Problem:</p>
        <p style={Style.wordWrap}>{item.problem}</p>
      </div>
    </List.Item>
  )

  renderDocs = () => {
    if (this.state.docs === undefined || this.state.docs.length === 0) {
      return Common.noDataHTML;
    } else {
      return (
        <div>
          <List dataSource={this.state.docs} renderItem={this.renderDocItem}/>
          <Pagination
            style={{float: 'right', marginTop: '20px'}}
            total={this.state.totalNum}
            current={this.state.page}
            pageSize={this.pageSize}
            onChange={this.onPageChange}
            showQuickJumper={true}
          />
          <div style={{clear: 'both'}}/>
        </div>
      );
    }
  }

  onPageChange = (page) => {
    this.setState({page: page});
    this.changeData(page);
  }

  changeData = (page: number) => {
    const data = {
      startTime: this.props.location.state.startTime,
      endTime: this.props.location.state.endTime,
      component: this.props.location.state.component,
      page: page,
      size: this.pageSize,
    };
    axios.post(`${Config.API_VERSION}/docs/condition`, data)
      .then(res => {
        const resHead = res.data.head;
        if (resHead.resultCode === Config.RES_SUCCESS) {
          const resData = res.data.data;
          this.setState({
            docs: resData.docs,
            totalNum: resData.total,
          });
        } else {
          notification.error({message: 'ERROR', description: resHead.message, duration: 3});
        }
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
    window.scrollTo(0, 0);
  }

  constructor(props: any) {
    super(props);
    this.state = {
      page: 1,
    };
    window.scrollTo(0, 0);
  }

  componentDidMount() {
    this.changeData(this.state.page);
  }

  render() {
    return (
      <div>
        <div>
          <h2 style={{fontWeight: 'bold', marginBottom: '10px', display: 'inline-block'}}>
            {this.props.location.state.component}
            :&nbsp;
          </h2>
          {this.props.location.state.startTime !== -1
          && this.props.location.state.endTime !== -1 && (
            <h2 style={{fontWeight: 'bold', marginBottom: '10px', display: 'inline-block'}}>
              {moment(this.props.location.state.startTime).format(Config.DATE_FORMAT)}
              &nbsp;~&nbsp;
              {moment(this.props.location.state.endTime).format(Config.DATE_FORMAT)}
            </h2>
          )}
        </div>
        <div style={Style.block}>
          {this.state.docs === undefined ? Common.loadingHTML : this.renderDocs()}
        </div>
      </div>
    );
  }
}
