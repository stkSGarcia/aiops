import { createStore } from 'redux';

const storeReducer = (state, action) => {
  switch (action.type) {
    case 'jstack':
      return {
        ...state,
        entryMap: action.entryMap,
        lockMap: action.lockMap,
      };
    case 'response':
      return {
        ...state,
        totalInfo: action.totalInfo,
        fileInfo: action.fileInfo,
        components: action.components,
      };
    case 'allHistory':
      return {
        ...state,
        allComponents: action.allComponents,
        singleFileInfo: action.singleFileInfo,
        allHistory: action.allHistory,
      };
    case 'reset':
      return undefined;
    default:
      return state;
  }
};

export const Store = createStore(storeReducer);
export default Store;
