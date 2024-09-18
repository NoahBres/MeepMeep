package com.noahbres.meepmeep.core.ui

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess

/**
 * A custom JFrame that sets up the main window for the application.
 *
 * @property title The title of the window.
 * @property windowX The width of the window.
 * @property windowY The height of the window.
 */
class WindowFrame(title: String, windowX: Int, windowY: Int) : JFrame() {
    private var internalWidth = windowX
    private var internalHeight = windowY

    // Main canvas for rendering
    val canvas = MainCanvas(internalWidth, internalHeight)

    // Panel to hold the canvas
    val canvasPanel = JPanel()

    init {
        // Set the title of the window
        setTitle(title)

        // Set the default close operation to do nothing
        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        // Add a window listener to handle the window closing event
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(we: WindowEvent?) {
                super.windowClosing(we)

                // Dispose of the window and exit the application
                dispose()
                exitProcess(0)
            }
        })

        // Set the size of thw window
        setSize(internalWidth, internalHeight)

        // Center the window on the screen
        setLocationRelativeTo(null)

        // Make the window not resizable
        isResizable = false

        // Set the layout of the content pane to BoxLayout along the X axis
        layout = BoxLayout(contentPane, BoxLayout.X_AXIS)

        // Set the layout of the canvas panel to BoxLayout along the Y axis
        canvasPanel.layout = BoxLayout(canvasPanel, BoxLayout.Y_AXIS)

        // Add the canvas to the canvas panel
        canvasPanel.add(canvas)

        // Add the canvas panel to the content pane
        contentPane.add(canvasPanel)

        // Pack the components within the window
        pack()

        // Start the canvas
        canvas.start()
    }
}
