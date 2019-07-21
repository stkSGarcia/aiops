import * as React from 'react';
import * as Style from '../Style';
import axios from 'axios';
import Config from './Config';
import { Button, Icon, notification, Upload, Col, Row } from 'antd';
import { UploadFile } from 'antd/lib/upload/interface';
import Store from './conf/CacheStore';
import { SarReportBean } from './conf/DataStructure';

const Dragger = Upload.Dragger;

interface SarState {
  fileList: UploadFile[];
  err: string;
  uploading: boolean;
  success: boolean;
  visibility: string[];
  analyzing: boolean;
}

export default class Sar extends React.Component<any, SarState> {
  handleSubmit = (e: any) => {
    e.preventDefault();
    if (this.state.fileList.length < 1) {
      this.setState({err: 'you must choose a file!'});
    } else {
      this.setState({
        uploading: true,
        success: false,
        analyzing: true,
      });

      this.state.fileList.forEach((f) => {
        const data = new FormData();
        data.set('file', f as any);
        axios.post(`${Config.API_VERSION}/sar`, data)
          .then(res => {
            this.setState({analyzing: false});
            if (res.data.head.resultCode === Config.RES_SUCCESS) {
              var sarReports = res.data.data.results as SarReportBean[];
              Store.dispatch({
                type: 'sarReportBean',
                sarReports: sarReports,
              });
              this.props.history.push({
                pathname: '/sar/dashboard',
              });
            } else {
              notification.error({message: 'ERROR', description: res.data.head.message, duration: 3});
            }
          })
          .catch(err => {
            this.setState({uploading: false, success: false, analyzing: false});
            notification.error({message: 'ERROR', description: err.message, duration: 3});
          });
      });
      this.setState({uploading: false, success: true});
    }
  }

  constructor(props: SarState) {
    super(props);
    this.state = {
      fileList: [],
      err: '',
      uploading: false,
      success: true,
      visibility: [],
      analyzing: false,
    };
  }

  componentWillMount() {
    var visibilityL: string[] = [];
    visibilityL.push('none');
    this.setState({visibility: visibilityL});
  }

  render() {
    const uploadProps = {
      anction: '',
      onRemove: (file: UploadFile) => {
        var fl = this.state.fileList;
        fl = fl.filter((v, i) => (v.name !== file.name));
        this.setState({
          fileList: fl,
        });
      },
      beforeUpload: (file: UploadFile) => {
        var fl = this.state.fileList;
        if (fl.filter((v, i) => (v.name === file.name)).length === 0) {
          fl = fl.concat(file);
        }
        this.setState({
          fileList: fl,
        });
        return false;
      },
      fileList: this.state.fileList,
      multiple: true
    };
    return (
      <div className="Sar" style={Style.uploadPage}>
        <h2 style={{marginBottom: '20px'}}>Sar 分析
          &nbsp;&nbsp;&nbsp;&nbsp;
          <span style={{fontSize: '16px'}}>详情 </span>
          <Icon
            key={'remind'}
            type={this.state.visibility[0] === 'none' ? 'down-circle-o' : 'up-circle-o'}
            style={{fontSize: '16px'}}
            onClick={() => {
              var v = this.state.visibility;
              if (v[0] === 'none') {
                v[0] = 'block';
              } else {
                v[0] = 'none';
              }
              this.setState({
                visibility: v,
              });
            }}
          />
          <Row key={'content'} type="flex" justify="start" style={{marginLeft: '40px', marginTop: '20px', width: '700px'}}>
            <Col style={{color: 'gray', fontSize: '16px', display: this.state.visibility[0]}}>
              <p>Sar（System Activity Reporter）: 是监控Linux系统各个性能的优秀工具，
                包括文件的读写情况、系统调用的使用情况、磁盘I/O、CPU效率、内存使用状况、进程活动及IPC有关的活动等。
                在系统级诊断中经常发挥主导作用，在大数据平台级诊断中也起到重要的辅助作用。
                <br/><br/>本系统会对Sar文件进行基本诊断，获取Sar文件的方式：
                <span style={{color: '#a84940'}}>"sar -A > sar.log"</span>
                <br/>获取sar历史的方式：
                <span style={{color: '#a84940'}}>"sar -A -f /var/log/sa/sa? > sar.log"</span></p>
            </Col>
          </Row>
        </h2>

        <Dragger {...uploadProps}>
          <p className="ant-upload-drag-icon"><Icon type="inbox"/></p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Do not exceed 10 files (zip format is supported)
          </p>
        </Dragger>
        <Button
          style={{marginTop: '20px'}}
          type="primary"
          onClick={this.handleSubmit}
          disabled={this.state.fileList.length === 0}
          loading={this.state.analyzing}
        >
          {this.state.analyzing ? 'Analyzing' : 'Start Analysis'}
        </Button>
      </div>
    );
  }
}
