import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*; // Librería para crear ventanas y componentes gráficos.
import java.awt.*; // Librería para gráficos 2D.
import java.io.File; // Para manejar archivos.
import javax.imageio.ImageIO; // Para leer imágenes.
import java.util.Random; // Para generar números aleatorios.
public class WackyRaces extends  JFrame{
    public static void main(String[] args) {
        // Punto de entrada del programa. Aquí arrancamos el juego.
        SwingUtilities.invokeLater(() -> new WackyRaces().setVisible(true));
    }

    // Constantes para que el código sea más claro y fácil de cambiar.
    private static final int NUMERO_COCHES = 4; // Hay 4 coches en la carrera.
    private static final int ANCHO_VENTANA = 800; // Ancho de la ventana.
    private static final int ALTO_VENTANA = 600; // Alto de la ventana.

    // Variables del juego
    private Image fondo; // La imagen de fondo (la carretera).
    private Image[] coches; // Guarda las imágenes de los coches.
    private final String[] nombres = {"Pussycat", "Rocomovil", "Superferrari", "Troncoswagen"}; // Nombres de los coches.
    private int[] posiciones; // Posiciones actuales de los coches en la carretera.
    private int[] velocidades; // Velocidades de cada coche.
    private boolean corriendo; // Si la carrera está en curso o no.
    private int distancia; // La distancia total que deben recorrer los coches.
    private Timer timer; // Temporizador para mover los coches cada cierto tiempo.
    private Clip musicaFondoClip; // Variable para la música de fondo.
    private boolean musicaPausada = false; // Estado de la música (si está pausada o no).

    public WackyRaces() {
        inicializarVentana(); // Configura la ventana principal.
        inicializarRecursos(); // Carga las imágenes y configura las variables de juego.
        inicializarJuego(); // Crea los botones y paneles para el juego.
        reproducirMusicaFondo();
    }

    // Método para reproducir la música de fondo.
    private void reproducirMusicaFondo() {
        try {
            // Ruta del archivo de música de fondo.
            File musicaArchivo = new File("Resources/musica_fondo.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
            musicaFondoClip = AudioSystem.getClip();
            musicaFondoClip.open(audioStream);
            musicaFondoClip.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce en bucle
            musicaFondoClip.start(); // Inicia la reproducción
        } catch (Exception e) {
            e.printStackTrace(); // Si ocurre un error, muestra el detalle
        }
    }

    // Método para pausar o reanudar la música de fondo
    private void pausarReanudarMusica() {
        if (musicaPausada) {
            musicaFondoClip.start(); // Reanudar música
            musicaPausada = false;
        } else {
            musicaFondoClip.stop(); // Pausar música
            musicaPausada = true;
        }
    }

    private void inicializarVentana() {
        // Configuración básica de la ventana del juego.
        setTitle("Juego de Coches"); // Título
        setSize(ANCHO_VENTANA, ALTO_VENTANA); // Tamaño
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar cuando se pulse "X".
        setLocationRelativeTo(null); // Centra la ventana en la pantalla.
        setResizable(false); // Establecemos que el tamaño de la ventana no puede cambiarse.
    }

    private void inicializarRecursos() {
        // Prepara las posiciones y velocidades de los coches.
        posiciones = new int[NUMERO_COCHES]; // Inicializa las posiciones en 0.
        velocidades = new int[NUMERO_COCHES]; // Inicializa las velocidades.

        coches = new Image[NUMERO_COCHES]; // imágenes de los coches.

        try {
            // Carga las imágenes del fondo y de los coches desde la carpeta "Resources".
            fondo = ImageIO.read(new File("Resources/carretera.PNG"));
            coches[0] = ImageIO.read(new File("Resources/Pussycat.png"));
            coches[1] = ImageIO.read(new File("Resources/Rocomovil.png"));
            coches[2] = ImageIO.read(new File("Resources/Superferrari.png"));
            coches[3] = ImageIO.read(new File("Resources/Troncoswagen.png"));
        } catch (Exception e) {
            // Si algo falla al cargar, imprime el error en la consola.
            e.printStackTrace();
        }
    }

    private void inicializarJuego() {
        // Crea los botones de "Iniciar" y "Reiniciar".
        JButton iniciarButton = new JButton("Iniciar");
        JButton reiniciarButton = new JButton("Reiniciar");
        JButton musicaButton = new JButton("Pausar Música");
        reiniciarButton.setEnabled(false); // Al inicio, no puedes reiniciar.

        // Define lo que ocurre cuando haces clic en los botones (llamada).
        iniciarButton.addActionListener(e -> iniciarCarrera(iniciarButton, reiniciarButton));
        reiniciarButton.addActionListener(e -> reiniciarCarrera(iniciarButton, reiniciarButton));
        musicaButton.addActionListener(e -> pausarReanudarMusica()); // Acción del botón de música.

        // Crea un panel para los botones y los añade.
        JPanel panelBotones = new JPanel();
        panelBotones.add(iniciarButton);
        panelBotones.add(reiniciarButton);
        panelBotones.add(musicaButton); // Añade el botón de música

        // Crea un panel principal y le añade los botones arriba y el dibujo en el centro.
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelBotones, BorderLayout.NORTH);

        // Crea el panel donde se dibujará la carrera.
        JPanel panelDibujo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                pintarEscena(g); // Llama al método para dibujar el juego.
            }
        };
        panelDibujo.setPreferredSize(new Dimension(ANCHO_VENTANA, 500));
        panelPrincipal.add(panelDibujo, BorderLayout.CENTER);

        add(panelPrincipal); // Añade todo
    }

    private void pintarEscena(Graphics g) {
        // Dibuja el fondo (la carretera).
        g.drawImage(fondo.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, this);

        // Define los carriles donde van los coches.
        int[] carriles = {310, 360, 410, 460}; // Coordenadas Y para cada coche.
        int cocheWidth = 80, cocheHeight = 50, margenCarril = 10; // Tamaños y márgenes.
        Color[] coloresBarras = {Color.PINK, Color.GRAY, Color.MAGENTA, Color.RED}; // Colores de las barras de progreso.

        for (int i = 0; i < NUMERO_COCHES; i++) {
            // Escala la posición del coche al ancho de la ventana.
            int posicionEscalada = (int) ((double) posiciones[i] / distancia * (getWidth() - cocheWidth));

            // Calcula el porcentaje de avance de cada coche.
            int porcentaje = Math.min((int) ((double) posiciones[i] / distancia * 100), 100);

            // Dibuja la barra de progreso.
            g.setColor(coloresBarras[i]);
            int barraLongitud = Math.min(posicionEscalada, getWidth() - cocheWidth - margenCarril);
            g.fillRect(margenCarril, carriles[i] + cocheHeight / 2 - 10, barraLongitud, 10);

            // Dibuja el texto del porcentaje encima de la barra.
            g.setColor(Color.WHITE);
            g.drawString(porcentaje + "%", barraLongitud - 30 + margenCarril, carriles[i] + cocheHeight / 2);

            // Dibuja el coche en su posición escalada.
            g.drawImage(coches[i].getScaledInstance(cocheWidth, cocheHeight, Image.SCALE_SMOOTH),
                    Math.min(posicionEscalada + margenCarril, getWidth() - cocheWidth), carriles[i], this);
        }
    }


    private void iniciarCarrera(JButton iniciarButton, JButton reiniciarButton) {
        // Pide la distancia de la carrera.
        String input = JOptionPane.showInputDialog(this, "Introduce los kilómetros a recorrer:");
        try {
            int distanciaKm = Integer.parseInt(input);
            distancia = Math.min(distanciaKm * 10, getWidth() - 100); // Convierte a píxeles.
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El número no es valido");
            return;
        }

        // Inicializa posiciones y velocidades aleatorias.
        Random random = new Random();
        for (int i = 0; i < NUMERO_COCHES; i++) {
            posiciones[i] = 0;
            velocidades[i] = random.nextInt(10) + 1;
        }

        // Activa la carrera.
        corriendo = true;
        iniciarButton.setEnabled(false);
        reiniciarButton.setEnabled(false);

        // Usa un temporizador para actualizar el juego cada 50 ms.
        timer = new Timer(50, e -> actualizarCarrera(iniciarButton, reiniciarButton));
        timer.start();
    }

    private void actualizarCarrera(JButton iniciarButton, JButton reiniciarButton) {
        // Mueve los coches y verifica si alguno llegó a la meta.
        boolean alguienGano = false;

        for (int i = 0; i < NUMERO_COCHES; i++) {
            posiciones[i] += velocidades[i];
            if (posiciones[i] >= distancia) {
                alguienGano = true;
                timer.stop();
                corriendo = false;
                mostrarGanador(i);
                break;
            }
        }

        repaint(); // Redibuja la escena.

        if (alguienGano) {
            iniciarButton.setEnabled(false);
            reiniciarButton.setEnabled(true);
        }
    }

    private void mostrarGanador(int indexGanador) {
        // Muestra quién ganó.
        JOptionPane.showMessageDialog(this, "¡Ha ganado el coche " + nombres[indexGanador] + "!");

        // Crea un StringBuilder para juntar las distancias recorridas de los coches no ganadores
        StringBuilder distancias = new StringBuilder();
        for (int i = 0; i < NUMERO_COCHES; i++) {
            if (i != indexGanador) {
                distancias.append("El coche " + nombres[i] + " recorrió " + posiciones[i] + "metros.\n");
            }
        }

        // Muestra todas las distancias de los coches no ganadores en una sola ventana
        JOptionPane.showMessageDialog(this, distancias.toString());


        // Mostrar el GIF y el sonido correspondiente
        if (indexGanador == 2) {
            mostrarGifYSonido("Resources/patan.gif", "Resources/risa.wav"); // Superferrari (patan)
        } else if (indexGanador == 0) {
            mostrarGifYSonido("Resources/penelope.gif", "Resources/ohdear.wav"); // Pussycat
        } else if (indexGanador == 1) {
            mostrarGifYSonido("Resources/slag_brothers.gif", "Resources/cuerno.wav"); // Rocomovil
        } else if (indexGanador == 3) {
            mostrarGifYSonido("Resources/brutus.gif", "Resources/timber.wav"); // Troncoswagen
        }

        if (musicaFondoClip != null && musicaFondoClip.isRunning()) {
            musicaFondoClip.stop(); // Detener la música
        }
    }

    // Método para mostrar el GIF y reproducir el sonido correspondiente
    private void mostrarGifYSonido(String rutaGif, String rutaSonido) {
        // Abre el GIF
        ImageIcon gif = new ImageIcon(rutaGif);
        JLabel gifLabel = new JLabel(gif);

        JFrame gifFrame = new JFrame("¡Ganador!");
        gifFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gifFrame.setSize(300, 300);
        gifFrame.setLocationRelativeTo(this);
        gifFrame.add(gifLabel);
        gifFrame.setVisible(true);

        // Reproduce el sonido asociado al GIF y obtiene la duración
        int duracionSonido = reproducirSonidoConDuracion(rutaSonido);

        // Ajusta el cierre del GIF a la duración del sonido o 3 segundos (lo que sea mayor)
        int duracionGif = Math.max(duracionSonido, 3000); // Mínimo 5 segundos para cerrar
        Timer timer = new Timer(duracionGif, e -> gifFrame.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private int reproducirSonidoConDuracion(String rutaSonido) {
        try {
            File archivoAudio = new File(rutaSonido); // Ruta del archivo WAV
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivoAudio);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Reproduce el audio
            clip.start();

            // Calcula la duración del audio en milisegundos
            int duracion = (int) (clip.getMicrosecondLength() / 1000);
            return duracion;

        } catch (Exception e) {
            e.printStackTrace(); // Si ocurre algún error, muestra el detalle
            return 5000; // Duración predeterminada de 5 segundos en caso de error
        }
    }


    private void reiniciarCarrera(JButton iniciarButton, JButton reiniciarButton) {
        // Reinicia el juego.
        corriendo = false;
        if (timer != null) timer.stop();
        for (int i = 0; i < NUMERO_COCHES; i++) {
            posiciones[i] = 0;
        }
        repaint(); // Limpia la pantalla.
        iniciarButton.setEnabled(true);
        reiniciarButton.setEnabled(false);
        reproducirMusicaFondo();// Reproduce la música de fondo nuevamente al reiniciar la carrera.
    }
}


