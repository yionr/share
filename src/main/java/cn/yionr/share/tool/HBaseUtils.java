package cn.yionr.share.tool;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Component
public class HBaseUtils {

    public static boolean CLEARED = false;

    Configuration conf;
    Connection conn = null;
    Admin admin = null;

    String table;
    public String dataColumnFamily;
    String trashColumnFamily;

    @Autowired
    public HBaseUtils(@Value("${table}") String table, @Value("${table.dataColumnFamily}") String dataColumnFamily, @Value("${table.trashColumnFamily}") String trashColumnFamily) throws IOException {
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "docker-hbase");
        conf.set("zookeeper.znode.parent", "/hbase");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conn = ConnectionFactory.createConnection(conf);
        admin = conn.getAdmin();

        this.table = table;
        this.dataColumnFamily = dataColumnFamily;
        this.trashColumnFamily = trashColumnFamily;

        //        filetype列族下有四列，file|text|image|trash
//        未来改造filetype列族，trash单独一个列族
//也可以给trash单独做个表

        if (!isExist())
            createTable(this.table, this.dataColumnFamily);
    }

    public void releaseTrash() throws IOException {
        ResultScanner trash = scan("trash");
        Iterator<Result> iterator = trash.iterator();
        while (iterator.hasNext()) {
            Result next = iterator.next();
            byte[] value = next.getValue(dataColumnFamily.getBytes(), "trash".getBytes());
            if (value.length != 0) {
                byte[] row = next.getRow();
                delete(new String(row));
            }
        }
        CLEARED = true;
    }

    public boolean isExist() throws IOException {
        return admin.tableExists(TableName.valueOf(this.table));
    }

    public boolean exists(String code) throws IOException {
        return !select(code).isEmpty();
    }
    public boolean inTrash(String code) throws IOException {
        return select(code).getValue(dataColumnFamily.getBytes(), "trash".getBytes()) != null;
    }

    /**
     * 创建数据表
     */
    public void createTable(String tableName, String... columnFamilies) throws IOException {
        TableName name = TableName.valueOf(tableName);
        if (!admin.isTableAvailable(name)) {
            //表描述器构造器
            TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(name);
            List<ColumnFamilyDescriptor> columnFamilyDescriptorList = new ArrayList<>();
            for (String columnFamily : columnFamilies) {
                //列族描述起构造器
                ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(columnFamily));
                //获得列描述
                ColumnFamilyDescriptor columnFamilyDescriptor = columnFamilyDescriptorBuilder.build();
                columnFamilyDescriptorList.add(columnFamilyDescriptor);
            }
            // 设置列簇
            tableDescriptorBuilder.setColumnFamilies(columnFamilyDescriptorList);
            //获得表描述器
            TableDescriptor tableDescriptor = tableDescriptorBuilder.build();
            //创建表
            admin.createTable(tableDescriptor);
        }
    }

    /**
     * 插入一条记录
     */
    public void insertOne(String rowKey, String column, String value) throws IOException {
        Put put = new Put(Bytes.toBytes(rowKey));
        //下面三个分别为，列族，列名，列值
        put.addColumn(Bytes.toBytes(dataColumnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
        //得到 table
        Table table = conn.getTable(TableName.valueOf(this.table));
        //执行插入
        table.put(put);
    }

    /**
     * 更新数据
     */

    public void update(String rowKey, String column, String value) throws IOException {
        Table table = conn.getTable(TableName.valueOf(this.table));
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(dataColumnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
        table.put(put);
    }

    /**
     * 删除单行单列
     */
    public void delete(String rowKey, String column) throws IOException {
        Table table = conn.getTable(TableName.valueOf(this.table));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        delete.addColumn(Bytes.toBytes(dataColumnFamily), Bytes.toBytes(column));
        table.delete(delete);
    }

    /**
     * 删除单行多列
     */
    public void delete(String tableName, String rowKey, String columnFamily, String... columnList) throws IOException {
        Table table = conn.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        for (String column : columnList) {
            delete.addColumns(Bytes.toBytes(columnFamily), Bytes.toBytes(column));
        }
        table.delete(delete);
    }

    /**
     * 删除单行
     */
    public void delete(String rowKey) throws IOException {
        Table table = conn.getTable(TableName.valueOf(this.table));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        table.delete(delete);
    }

    /**
     * 查询表
     */
    public Result select(String rowKey) throws IOException {
        Table table = conn.getTable(TableName.valueOf(this.table));
        Get get = new Get(Bytes.toBytes(rowKey));
        return table.get(get);

    }

    /**
     * 全表扫描
     */
    public ResultScanner scan() throws IOException {
        Table table = conn.getTable(TableName.valueOf(this.table));
        Scan scan = new Scan();

        return table.getScanner(scan);
    }

    public List<String> scanRowKey() throws IOException {
        List<String> list = new ArrayList<>();
        Table table = conn.getTable(TableName.valueOf(this.table));
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            list.add(new String(result.getRow()));
        }
        return list;
    }

    /**
     * 全表扫描-列
     */
    public ResultScanner scan(String column) throws IOException {
        Table table = conn.getTable(TableName.valueOf(this.table));
        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes(dataColumnFamily), Bytes.toBytes(column));

        return table.getScanner(scan);
    }


    /**
     * 关闭连接
     */
    public void close() {
        try {
            conn.close();
        } catch (IOException e) {
            conn = null;
        } finally {
            conn = null;
        }
    }
}
