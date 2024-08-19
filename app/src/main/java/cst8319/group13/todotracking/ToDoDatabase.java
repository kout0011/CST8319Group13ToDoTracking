package cst8319.group13.todotracking;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {User.class}, version = 1)
public abstract class ToDoDatabase extends RoomDatabase {

    private static ToDoDatabase INSTANCE;

    public abstract UserDao userDao();

    public static synchronized ToDoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ToDoDatabase.class, "todo_database")
                    .allowMainThreadQueries()  // 允许在主线程上查询数据库（仅限调试用途）
                    .fallbackToDestructiveMigration()
                    .build();

            // 添加日志以调试数据库创建
            Log.d("DatabaseDebug", "Database has been created");
        }
        return INSTANCE;
    }
}