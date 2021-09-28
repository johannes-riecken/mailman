/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package appscripter;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author sky
 */
public class Main {
    public Main() {
    }

    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
        List<DemoStage> stages = DemoParser.parse(Main.class.getResourceAsStream("resources/script.xml"));

        System.err.println("pre");
        for (DemoStage stage : stages) {
            System.err.println("stage");
            for (DemoMessage msg : stage.getMessages()) {
                System.err.println("\t" + msg.getMessage());
            }
        }
        stages = DemoController.flowStages(stages, 500);
        System.err.println("post");
        for (DemoStage stage : stages) {
            System.err.println("stage");
            for (DemoMessage msg : stage.getMessages()) {
                System.err.println("\t" + msg.getMessage() + " font=" + msg.getFont());
            }
        }
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final DemoController controller = new DemoController(frame, stages);
        frame.setBounds(0, 0, 600, 600);
        frame.setVisible(true);
        Timer timer = new Timer(600, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.start();
            }
        });
        timer.setRepeats(false);
        timer.start();
//        DemoFrame testFrame = new DemoFrame();
//        final DemoGlassPane gp = testFrame.getGlassPane();
//        gp.setVisible(true);
//        gp.appendMessage(new Font("Arial", Font.PLAIN, 48), "Test");
//        gp.appendMessage(new Font("Arial", Font.PLAIN, 36), "Test");
//        testFrame.setBounds(0, 0, 400, 400);
//        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        testFrame.show();
//        Timer timer = new Timer(2000, new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                TimingController tc = new TimingController(600, new TimingTarget() {
//                    public void begin() {
//                    }
//                    public void end() {
//                    }
//                    public void timingEvent(long l, long l0, float percent) {
//                        gp.setLabelTranslucency(1f - percent);
////                        gp.getMessages().get(1).setAlpha(1f - percent);
//                    }
//                });
//                tc.start();
//            }
//        });
//        timer.setRepeats(false);
//        timer.start();
    }
}
