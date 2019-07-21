import * as React from 'react';
import * as Style from '../../Style';
import ExpandableDiv from '../../util/ExpandableDiv';
import { List } from 'antd';
import { DocItem } from '../conf/DataStructure';

interface DocListProps {
  data: DocItem[];
}

export default class DocList extends React.Component<DocListProps, any> {
  renderItem = (item: DocItem, index: number) => (
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

  render() {
    return (
      <ExpandableDiv>
        <List dataSource={this.props.data} renderItem={this.renderItem}/>
      </ExpandableDiv>
    );
  }
}
