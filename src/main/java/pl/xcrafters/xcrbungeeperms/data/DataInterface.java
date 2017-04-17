package pl.xcrafters.xcrbungeeperms.data;

public interface DataInterface {

    public void setPrimary(int id);
    public int getPrimary();
    
    public void insert();
    public void update();
    public void delete();
    
    public String prepareQuery(DataManager.QueryType type);
    
    public String getUpdateChannel(DataManager.QueryType type);

    public void synchronize();
    
}
