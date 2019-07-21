import * as React from 'react';
import * as Style from '../Style';
import Config from '../Config';
import axios from 'axios';
import { Button, Icon, notification, Upload } from 'antd';
import { AnalyzeResponse, JstackResponse } from './conf/DataStructure';
import Store from './conf/CacheStore';

const Dragger = Upload.Dragger;

interface UploaderState {
  fileList: any[];
  analyzing: boolean;
}

export default class Uploader extends React.Component<any, UploaderState> {
  handleUpload = () => {
    const data = new FormData();
    this.state.fileList.forEach(file => data.append('files', file));
    this.setState({analyzing: true});

    axios.post(`${Config.API_VERSION}/jstack`, data, {timeout: Config.REQUEST_TIMEOUT})
      .then(res => {
        this.setState({analyzing: false});
        if (res.data.head.resultCode === Config.RES_SUCCESS) {
          const resData = res.data.data as JstackResponse;
          Store.dispatch({
            type: 'response',
            totalInfo: resData.totalInfo,
            fileInfo: resData.fileInfo,
            components: resData.components,
          });
          this.props.history.push({
            pathname: '/jstack/dashboard',
          });
        } else {
          notification.error({message: 'ERROR', description: res.data.head.message, duration: 3});
        }
      })
      .catch(err => {
        this.setState({analyzing: false});
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  handleJstack = () => {
    const filename: string = '1_2019-01-21_13-10-01_1';
    axios.get(`${Config.API_VERSION}/jstack/analysis`, {params: {filename: filename}})
      .then(res => {
        if (res.data.head.resultCode === Config.RES_SUCCESS) {
          const resData = res.data.data as AnalyzeResponse;
          Store.dispatch({
            type: 'allHistory',
            allComponents: resData.components,
            singleFileInfo: resData.fileInfo,
            allHistory: resData.allHistory,
          });
          this.props.history.push({
            pathname: '/jstack/history_dashboard',
          });
        } else {
          notification.error({message: 'ERROR', description: res.data.head.message, duration: 3});
        }
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });

  }

  constructor(props: any) {
    super(props);
    this.state = {
      fileList: [],
      analyzing: false,
    };
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
      <div style={Style.uploadPage}>
        <h2 style={{marginBottom: '20px'}}>Jstack 分析 </h2>
        <Dragger {...uploadProps}>
          <p className="ant-upload-drag-icon"><Icon type="inbox"/></p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for a single or bulk upload. Strictly prohibit from uploading company data or other band files
          </p>
        </Dragger>

        <Button
          style={{marginTop: '20px'}}
          type="primary"
          onClick={this.handleUpload}
          disabled={this.state.fileList.length === 0}
          loading={this.state.analyzing}
        >
          {this.state.analyzing ? 'Analyzing' : 'Start Analysis'}
        </Button>

        <br/>
        <Button
          style={{marginTop: '20px'}}
          type="primary"
          onClick={this.handleJstack}
        >
          对接
        </Button>
      </div>
    );
  }
}
