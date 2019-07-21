import * as React from 'react';
import * as LogStyle from '../conf/LogStyle';
import { BackTop, List } from 'antd';
import { LogEntity } from '../conf/DataStructure';

export default class Logs extends React.Component<any, any> {
  entities: LogEntity[] = this.props.location.state.entities;

  render() {
    const renderItem = (item: LogEntity, index: number) => (
      <List.Item key={index} style={LogStyle.entity(item.level)}>
        <div id={index.toString()} style={LogStyle.wrap}>
          {item.content.split('\n').map((value, id) =>
            id === 0 ? <p style={{margin: 0}} key={id}>{index + 1}: {value}</p> :
              <p style={{margin: 0}} key={id}>{value}</p>
          )}
        </div>
      </List.Item>
    );
    return (
      <div style={LogStyle.page}>
        <BackTop/>
        <List dataSource={this.entities} renderItem={renderItem}/>
      </div>
    );
  }
}
