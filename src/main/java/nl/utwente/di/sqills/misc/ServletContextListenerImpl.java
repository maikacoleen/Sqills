package nl.utwente.di.sqills.misc;

import nl.utwente.di.sqills.dao.ReservationDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Timer;

public class ServletContextListenerImpl implements ServletContextListener {
    private Timer timer;

    public ServletContextListenerImpl() {
        timer = new Timer();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("nl.utwente.di.sqills.misc.C3P0");
            Class.forName("nl.utwente.di.sqills.misc.CalendarClient");
            Class.forName("nl.utwente.di.sqills.dao.RoomDAO");
            Class.forName("nl.utwente.di.sqills.dao.EmployeeDAO");
            Class.forName("nl.utwente.di.sqills.dao.ReservationDAO");
            Class.forName("nl.utwente.di.sqills.dao.AccountDAO");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        timer.schedule(ReservationDAO.INSTANCE.getTimerTask(), 0, 1000);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        timer.cancel();
        C3P0.INSTANCE.close();
    }
}
