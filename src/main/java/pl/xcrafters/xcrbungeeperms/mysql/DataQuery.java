package pl.xcrafters.xcrbungeeperms.mysql;

import pl.xcrafters.xcrbungeeperms.data.DataInterface;
import pl.xcrafters.xcrbungeeperms.data.DataManager.QueryType;

public class DataQuery {

    public DataQuery(DataInterface data, QueryType type){
        this.data = data;
        this.type = type;
    }
    
    public DataInterface data;
    public QueryType type;
    
}
