import java.awt.EventQueue;

public class W09PracticalExt {
    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            GUI ex = new GUI();
            ex.setVisible(true);
        });

    }
}
