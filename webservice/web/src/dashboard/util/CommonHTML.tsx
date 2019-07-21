import * as React from 'react';
import { Icon, Spin } from 'antd';

export const loadingHTML = (
  <div style={{textAlign: 'center'}}>
    <Spin tip="Loading..." indicator={<Icon type="loading"/>} size="large"/>
  </div>
);
export const noDataHTML = <div style={{fontWeight: 'bold', fontSize: '16px', textAlign: 'center', color: '#bfbfbf'}}>NO DATA</div>;
