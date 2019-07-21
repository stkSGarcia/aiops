import * as React from 'react';
import * as Style from '../Style';
import * as LogStyle from './conf/LogStyle';
import './conf/LogStyle.css';
import Store from './conf/CacheStore';
import Config from './../Config';
import axios from 'axios';
import { Button, Icon, notification, Progress, Upload } from 'antd';
import { LogInitResponse } from './conf/DataStructure';

const Dragger = Upload.Dragger;

enum Status {
  Ready, Upload, Unzip, Analyze, Done
}

interface UploaderState {
  fileList: any[];
  status: Status;
  percent: number;
  isDone: any;
}

export default class Uploader extends React.Component<any, UploaderState> {
  handleUpload = () => {
    const fileList = this.state.fileList;
    const data = new FormData();
    fileList.forEach(file => data.append('files[]', file));
    this.setState({status: Status.Upload, percent: 0});

    const config = {
      onUploadProgress: event => {
        this.setState({percent: Math.round((event.loaded * 100) / event.total)});
      }
    };
    axios.post('/api/log/upload', data, config)
      .then(res => {
        if (res.data === 'ok') {
          this.stateDone(Status.Upload);
          this.checkStatus();
        }
      })
      .catch(err => {
        this.setState({status: Status.Ready});
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  checkStatus = async () => {
    let isDone = false;
    let response;
    while (!isDone) {
      const data = (await axios.post('/api/log/init')).data as LogInitResponse;
      switch (data.status) {
        case 'unzip':
          this.setState({
            status: Status.Unzip,
            percent: Math.round(data.rate * 100),
          });
          this.stateDone(Status.Upload);
          break;
        case 'analyse':
          this.setState({
            status: Status.Analyze,
            percent: Math.round(data.rate * 100),
          });
          this.stateDone(Status.Unzip);
          break;
        case 'ok':
          this.setState({
            status: Status.Done,
            percent: 100,
          });
          this.stateDone(Status.Analyze);
          response = data.compSet;
          isDone = true;
          break;
        default:
      }
      await this.sleep(Config.POLLING_INTERVAL);
    }
    this.props.history.push({
      pathname: '/log/dashboard',
      state: {
        compSet: response,
      }
    });
  }

  stateDone = (state: Status) => {
    const isDone = this.state.isDone;
    isDone[state] = true;
    this.setState({isDone: isDone});
  }

  sleep = (ms: number) => {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  constructor(props: any) {
    super(props);
    this.state = {
      fileList: [],
      status: Status.Ready,
      percent: 0,
      isDone: {
        [Status.Upload]: false,
        [Status.Unzip]: false,
        [Status.Analyze]: false,
      },
    };
  }

  componentDidMount() {
    Store.dispatch({type: 'reset'});
  }

  render() {
    const uploadProps = {
      name: 'file',
      multiple: true,
      fileList: this.state.fileList,
      onRemove: (file) => {
        this.setState(({fileList}) => {
          const index = fileList.indexOf(file);
          const newFileList = fileList.slice();
          newFileList.splice(index, 1);
          return {fileList: newFileList};
        });
      },
      beforeUpload: (file) => {
        this.setState(({fileList}) => {
          if (fileList.filter(v => v.name === file.name).length === 0) {
            return {fileList: [...fileList, file]};
          } else {
            return {fileList: fileList};
          }
        });
        return false;
      },
    };
    return (
      <div className="Log" style={Style.uploadPage}>
        <h2 style={{marginBottom: '20px'}}>Log 分析</h2>
        <Dragger {...uploadProps}>
          <p className="ant-upload-drag-icon"><Icon type="inbox"/></p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for a single or bulk upload. Strictly prohibit from uploading company data or other band files
          </p>
        </Dragger>
        {this.state.status !== Status.Ready && (
          <div style={{marginTop: '20px'}}>
            {this.state.isDone[Status.Upload] ? <p style={{color: LogStyle.green.color}}>Uploaded</p> :
              this.state.status === Status.Upload && <p style={{color: LogStyle.blue.color}}>Uploading</p>}
            {this.state.isDone[Status.Unzip] ? <p style={{color: LogStyle.green.color}}>Unzipped</p> :
              this.state.status === Status.Unzip && <p style={{color: LogStyle.blue.color}}>Unzipping</p>}
            {this.state.isDone[Status.Analyze] ? <p style={{color: LogStyle.green.color}}>Analyzed</p> :
              this.state.status === Status.Analyze && <p style={{color: LogStyle.blue.color}}>Analyzing</p>}
            <Progress style={{marginRight: '100px'}} percent={this.state.percent}/>
          </div>)}
        <Button
          style={{marginTop: '20px'}}
          type="primary"
          onClick={this.handleUpload}
          disabled={this.state.fileList.length === 0}
          loading={this.state.status !== Status.Ready}
        >
          {this.state.status === Status.Ready ? 'Start Upload' :
            this.state.status === Status.Upload ? 'Uploading' :
              this.state.status === Status.Unzip ? 'Unzipping' :
                this.state.status === Status.Analyze ? 'Analyzing' :
                  'Done'}
        </Button>
      </div>
    );
  }
}
