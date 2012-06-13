package org.kevoree.extra.kserial.SerialPort;

import com.sun.jna.Memory;
import com.sun.jna.ptr.PointerByReference;
import org.kevoree.extra.kserial.CommPort;
import org.kevoree.extra.kserial.Constants;
import org.kevoree.extra.kserial.Utils.ByteFIFO;
import org.kevoree.extra.kserial.Utils.KHelpers;
import org.kevoree.extra.kserial.jna.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 26/01/12
 * Time: 17:29

 * SerialPort
 */

public class SerialPort extends CommPort {

    private SerialPortEvent SerialPortEvent;
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private boolean exit = false;
    private int sizefifo_out = 1024;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ByteFIFO fifo_out = new ByteFIFO(sizefifo_out);

    public SerialPort (String portname, int bitrate) throws Exception {

        if(portname.equals("*"))
        {
            if(KHelpers.getPortIdentifiers().size() > 0)
            {
                this.setPort_name(KHelpers.getPortIdentifiers().get(0));
            }
            else
            {
                this.setPort_name("null");
            }

        } else {
            this.setPort_name(portname);
        }
        this.setPort_bitrate(bitrate);

    }

    public void addEventListener (SerialPortEventListener listener) {
        if(listenerList != null){
            listenerList.add(SerialPortEventListener.class, listener);
        }
    }

    public void removeEventListener (SerialPortEventListener listener) {
        if(listenerList != null){
            listenerList.remove(SerialPortEventListener.class, listener);
        }
    }

    void fireSerialEvent (SerialPortEvent evt)
    {
        if(listenerList !=null ) {
            Object[] listeners = listenerList.getListenerList();
            if(listeners != null)
            {
                for (int i = 0; i < listeners.length; i += 2) {
                    if (listeners[i] == SerialPortEventListener.class)
                    {
                        if (evt instanceof SerialPortDisconnectionEvent)
                        {
                            ((SerialPortEventListener) listeners[i + 1]).disconnectionEvent((SerialPortDisconnectionEvent)evt);
                        } else {
                            ((SerialPortEventListener) listeners[i + 1]).incomingDataEvent(evt);
                        }
                    }
                }
            }
        }

    }

    public void storeData(byte[]bs){
        try
        {
            fifo_out.add(bs);
        } catch (InterruptedException e) {
            logger.error("The program has failed to backup data "+new String(bs));
        }
    }


    public void send(byte [] bs)
    {
        Memory mem = new Memory(Byte.SIZE * bs.length + 1);
        mem.clear();

        PointerByReference inipar = new PointerByReference();
        inipar.setPointer(mem);
        for (int i = 0; i < bs.length; i++) {
            inipar.getPointer().setByte(i * Byte.SIZE / 8, bs[i]);
        }
        byte c = '\n';
        inipar.getPointer().setByte((bs.length + 1) * Byte.SIZE / 8, c);
        if (NativeLoader.getINSTANCE_SerialPort().serialport_write(getFd(), inipar) != 0)
        {
            logger.error("Warning fail to write store " + bs.length);
            storeData(bs);
        }
    }

    @Override
    public void write (byte[] bs)
    {
        if (this.getFd() > 0)
        {
            if(fifo_out.getSize() > 0)
            {
                send(fifo_out.removeAll());
            }
            send(bs);
        } else
        {
            logger.error("The program has failed write "+new String(bs)+" "+bs.length+" octects on "+this.getPort_name());
            storeData(bs);
        }
    }

    public String getTmpfilePath(){
        String path_tmpdir = System.getProperty("java.io.tmpdir");
        String name =this.getPort_name().replace("/","");
        return path_tmpdir+"/"+name+"lock";
    }

    @Override
    public void open () throws SerialPortException
    {
        File   tmpfile=null;
        try
        {
            tmpfile = new File(getTmpfilePath());
            if(tmpfile.exists())
            {
                logger.warn("The serial port is locked " + tmpfile.getAbsolutePath());
                close();
            }else
            {
                logger.debug("Creating lock file "+tmpfile.getAbsolutePath());
                tmpfile.createNewFile();
                tmpfile.deleteOnExit();
            }
            setFd(NativeLoader.getINSTANCE_SerialPort().open_serial(this.getPort_name(), this.getPort_bitrate()));
            if (getFd() < 0) {
                NativeLoader.getINSTANCE_SerialPort().close_serial(getFd());
                throw new SerialPortException(this.getPort_name()+"- [" + getFd() + "] " + Constants.messages.get(getFd())+" Ports : "+ KHelpers.getPortIdentifiers());
            }
            else
            {
                SerialPortEvent = new SerialPortEvent(this);
            }

        }catch (Exception e)
        {
            if(tmpfile !=null)
            {

                tmpfile.delete();
            }
            close();
            throw  new SerialPortException(e.getMessage());
        }
    }

    public boolean autoReconnect (int tentative,SerialPortEventListener event) throws SerialPortException {
        close();
        removeEventListener(event);
        int i=0;
        while(i < tentative && exit == false)   {
            try
            {
                try
                {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //ignore
                }
                open();
                addEventListener(event);
                return true;
            }catch (Exception e){
                logger.error("Try ["+i+"/"+tentative+" ]-> the program has not successfully reconnect automatically to port " + this.getPort_name());
                close();
            }
            i++;
        }
        return false;
    }

    @Override
    public void close () throws SerialPortException {
        File   tmpfile=null;
        try
        {
            NativeLoader.getINSTANCE_SerialPort().close_serial(getFd());

            tmpfile = new File(getTmpfilePath());
            if(tmpfile != null && tmpfile.exists())
            {
                logger.debug("Removing lock file "+tmpfile.getAbsolutePath());
                tmpfile.delete();
            }
        }catch (Exception e)
        {
            logger.error("Closing serial port ",e);
        }
    }

    public void exit()
    {
        exit = true;
        try
        {
            close();
            listenerList = null;
            SerialPortEvent =null;
            fifo_out = null;
        } catch (SerialPortException e) {
            // ignore
        }
    }

}
