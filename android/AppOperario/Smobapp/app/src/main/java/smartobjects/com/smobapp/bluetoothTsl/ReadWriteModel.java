//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package smartobjects.com.smobapp.bluetoothTsl;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.uk.tsl.rfid.asciiprotocol.commands.BatteryStatusCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.WriteTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartobjects.com.smobapp.ActivityAuditoria;
import smartobjects.com.smobapp.ActivityAuditoriaDos;
import smartobjects.com.smobapp.objects.ObjectItem;

public class ReadWriteModel extends ModelBase {

    public static ArrayList<ObjectItem> mItemsEncontrados = new ArrayList<>();

    // The instances used to issue commands
    private final ReadTransponderCommand  mReadCommand = ReadTransponderCommand.synchronousCommand();
    private final WriteTransponderCommand mWriteCommand = WriteTransponderCommand.synchronousCommand();
    private final BatteryStatusCommand    mBatteryCommand = BatteryStatusCommand.synchronousCommand();
    // The inventory command configuration
    public ReadTransponderCommand getReadCommand() {
        return mReadCommand;
    }

    public WriteTransponderCommand getWriteCommand() {
        return mWriteCommand;
    }

    public BatteryStatusCommand getBatteryCommand() { return mBatteryCommand; }

    List<ObjectItem> mListaItems;

    private int mTransponderCount;

    /**
     * A class to demonstrate the use of the AsciiProtocol library to read and write to transponders
     */
    public ReadWriteModel() {
        mReadCommand.setOffset(0);
        mReadCommand.setLength(1);
        mWriteCommand.setOffset(0);
        mWriteCommand.setLength(1);
    }

    //----------------------------------------------------------------------------------------------
    // Read
    //----------------------------------------------------------------------------------------------

    // Set the parameters that are not user-specified
    private void setFixedReadParameters() {
        mReadCommand.setResetParameters(TriState.YES);

        // Configure the select to match the given EPC
        // EPC is in hex and length is in bits
        String epcHex = mReadCommand.getSelectData();

        if (epcHex == null || epcHex.length() == 0) {
            // Match anything by not selecting tags and querying the default A state
            mReadCommand.setInventoryOnly(TriState.YES);

            mReadCommand.setQuerySelect(QuerySelect.ALL);
            mReadCommand.setQuerySession(QuerySession.SESSION_0);
            mReadCommand.setQueryTarget(QueryTarget.TARGET_A);

            // Reset other properties used when matching
            mReadCommand.setSelectData(null);
            mReadCommand.setSelectOffset(-1);
            mReadCommand.setSelectLength(-1);
            mReadCommand.setSelectAction(SelectAction.NOT_SPECIFIED);
            mReadCommand.setSelectTarget(SelectTarget.NOT_SPECIFIED);
        } else {
            mReadCommand.setInventoryOnly(TriState.NO);

            // Only match the EPC value not the CRC or PC
            mReadCommand.setSelectOffset(0x20);
            mReadCommand.setSelectLength(epcHex.length() * 4);

            // Use session with long persistence and select tags away from default state
            mReadCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
            mReadCommand.setSelectTarget(SelectTarget.SESSION_2);

            mReadCommand.setQuerySelect(QuerySelect.ALL);
            mReadCommand.setQuerySession(QuerySession.SESSION_2);
            mReadCommand.setQueryTarget(QueryTarget.TARGET_B);
        }

        mReadCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                byte[] data = transponder.getReadData();
                String dataMessage = (data == null) ? "No data!" : HexEncoding.bytesToString(data);
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : "\n" + transponder.getAccessErrorCode().getDescription() + " (EA)";
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : "\n" + transponder.getBackscatterErrorCode().getDescription() + " (EB)";
                String errorMsg = eaMsg + ebMsg;
                if (errorMsg.length() > 0) {
                    errorMsg = "Error: " + errorMsg + "\n";
                }
//				sendMessageNotification(String.format(
////						"\nEPC: %s\nData: %s\n%s",
//						"%s,",
//						transponder.getEpc()
////						dataMessage,
////						errorMsg
//						));
                if( mHandler != null )
                {
                    Message msg = mHandler.obtainMessage(99, transponder.getEpc());
                    mHandler.sendMessage(msg);
                }
                try {
                    mItemsEncontrados.add(new ObjectItem(transponder.getEpc()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ++mTransponderCount;

				if( !moreAvailable) {
//                    sendMessageNotification("EPC encontrados : " + mItemsEncontrados.size());
//                    mItemsEncontrados.clear();
//					sendMessageNotification("\n\n" + "----------------------------------------------------------\n" + "\n");
				}
            }
        });
    }

    /**
     * Permite agregar los epc distntos en una lista.
     *
     * @param mResultTextView
     * @param fragmentItemsMaster
     * @param fragmentItemsDetails
     * @throws Exception
     */
    private synchronized void addEpc(TextView mResultTextView, ActivityAuditoria.FragmentItemsMaster fragmentItemsMaster, ActivityAuditoria.FragmentItemsDetails fragmentItemsDetails) throws Exception {
        //Obtenemos los items actuales
        List<ObjectItem> listaItemActuales = mItemsEncontrados;
        //Cremos una nueva lista limpia que guardará la nueva información
        List<ObjectItem> listaItemLimpia = new ArrayList<>();
        //Set para almacenar la información, teniendo como índice el EPC
        Map<String, ObjectItem> mapLista = new HashMap<String, ObjectItem>(listaItemActuales.size());
        //Recorremos la lista que tiene todos los datos, y le colocamos la información al map
        try {
            for (ObjectItem p : listaItemActuales) {
                mapLista.put(p.getEpc(), p);
            }
            //Quitamos los campos repetdidos
            for (Map.Entry<String, ObjectItem> p : mapLista.entrySet()) {
                listaItemLimpia.add(p.getValue());
            }
            //Limpiamos los items actuales
            mItemsEncontrados.clear();
            //Colocamos la nueva lista con todos los campos sin repetirse.
            mItemsEncontrados.addAll(listaItemLimpia);
            //Validamos que la nueva lista tenga información
            if (null != mItemsEncontrados && mItemsEncontrados.size() > 0) {
                //Recorremos toda la lista con la nueva información
                for (ObjectItem itemEncontrados : mItemsEncontrados) {
                    //Validamos con la lista final que se está usando para los demás
//					for (ObjectItem itemExistente : ObjectItem.getmListaItem()){
                    for (ObjectItem itemExistente : mListaItems) {
                        //Si encontramos los mismos EPC, entonces, le cambiamos el estado.
                        if (itemExistente.getEpc().equals(itemEncontrados.getEpc())) {
                            itemExistente.setEstatus(ObjectItem.ESTADO_ENCONTRADO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Liberamos las variables.
            listaItemActuales = null;
            listaItemLimpia = null;
            mapLista = null;
        }
    }


    public void read(final TextView mResultTextView, final ActivityAuditoria.FragmentItemsMaster fragmentItemsMaster, final ActivityAuditoria.FragmentItemsDetails fragmentItemsDetails) {
        try {
            setFixedReadParameters();
            //Primer mensaje que envía ala ventana
//			sendMessageNotification("\nReading...\n");
            //Se encarga de colocar toda la infomación que encuentre.
            if (null != mResultTextView) {

                mResultTextView.setText("");
            }
//			mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {

                    getCommander().executeCommand(mReadCommand);
//					sendMessageNotification("\nTransponders seen: " + mTransponderCount +"\n");
//					reportErrors(mReadCommand);
//					sendMessageNotification( String.format("Time taken: %.2fs", getTaskExecutionDuration()) );

                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        } finally {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        addEpc(mResultTextView, fragmentItemsMaster, fragmentItemsDetails);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
        }


    }

    //----------------------------------------------------------------------------------------------
    // Write
    //----------------------------------------------------------------------------------------------

    // Set the parameters that are not user-specified
    private void setFixedWriteParameters() {
        mWriteCommand.setResetParameters(TriState.YES);

        // Set the data length
        if (mWriteCommand.getData() == null) {
            mWriteCommand.setLength(0);
        } else {
            mWriteCommand.setLength(mWriteCommand.getData().length / 2);
        }

        // Configure the select to match the given EPC
        // EPC is in hex and length is in bits
        String epcHex = mWriteCommand.getSelectData();

        if (epcHex != null) {
            // Only match the EPC value not the CRC or PC
            mWriteCommand.setSelectOffset(0x20);
            mWriteCommand.setSelectLength(epcHex.length() * 4);
        }

        mWriteCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
        mWriteCommand.setSelectTarget(SelectTarget.SESSION_2);

        mWriteCommand.setQuerySelect(QuerySelect.ALL);
        mWriteCommand.setQuerySession(QuerySession.SESSION_2);
        mWriteCommand.setQueryTarget(QueryTarget.TARGET_B);

        mWriteCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : "\n" + transponder.getAccessErrorCode().getDescription() + " (EA)";
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : "\n" + transponder.getBackscatterErrorCode().getDescription() + " (EB)";
                String errorMsg = eaMsg + ebMsg;
                if (errorMsg.length() > 0) {
                    errorMsg = "Error: " + errorMsg + "\n";
                }

                sendMessageNotification(String.format(
                        "\nEPC: %s\nWords Written: %d of %d\n%s",
                        transponder.getEpc(),
                        transponder.getWordsWritten(), mWriteCommand.getLength(),
                        errorMsg
                ));
                ++mTransponderCount;

                if (!moreAvailable) {
                    sendMessageNotification("\n");
                }
            }
        });
    }


    public void write() {
        try {
            sendMessageNotification("\nWriting...\n");

            setFixedWriteParameters();
            mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {

                    getCommander().executeCommand(mWriteCommand);

                    sendMessageNotification("\nTransponders seen: " + mTransponderCount + "\n");
                    reportErrors(mWriteCommand);
                    sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));

                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }

    }

    /**
     * Check the given command for errors and report them via the model message system
     *
     * @param command The command to check
     */
    private void reportErrors(AsciiSelfResponderCommandBase command) {
        if (!command.isSuccessful()) {
            sendMessageNotification(String.format(
                    "%s failed!\nError code: %s\n", command.getClass().getSimpleName(), command.getErrorCode()));
            for (String message : command.getMessages()) {
                sendMessageNotification(message + "\n");
            }
        }

    }

    public void read(ActivityAuditoriaDos activityAuditoriaDos, List<ObjectItem> items) {
        if (mListaItems != null) {
            mListaItems.clear();
        } else {
            mListaItems = new ArrayList<>();
        }
        mListaItems.addAll(items);
        try {
            setFixedReadParameters();
            //Primer mensaje que envía ala ventana
//			sendMessageNotification("\nReading...\n");
            //Se encarga de colocar toda la infomación que encuentre.
//			mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {
                    if (getCommander().isConnected()) {
                        getCommander().executeCommand(mReadCommand);
                    }
//					sendMessageNotification("\nTransponders seen: " + mTransponderCount +"\n");
//					reportErrors(mReadCommand);
//					sendMessageNotification( String.format("Time taken: %.2fs", getTaskExecutionDuration()) );

                }
            });

        } catch (ModelException e) {
//            sendMessageNotification("Unable to perform action: " + e.getMessage());
        } catch (Exception e) {
//            sendMessageNotification("Unable to perform action: " + e.getMessage());
        } finally {
//			Handler handler = new Handler();
//			handler.post(new Runnable() {
//				@Override
//				public void run() {
            try {
                addEpc(null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
//				}
//			});
        }
    }

    /**
     * Método que se encarga de limpiar los items que ya han sido encontrados.
     */
    public void clearListItemsFounds() {
        if (null != mItemsEncontrados && mItemsEncontrados.size() > 0) {
            mItemsEncontrados.clear();
        }
    }

    public void read() {
        try {
//			sendMessageNotification("\nReading...\n");
            setFixedReadParameters();
//            mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {

                    getCommander().executeCommand(mReadCommand);
//                    getCommander().executeCommand(mBatteryCommand);

//					sendMessageNotification("\nTransponders seen: " + mTransponderCount + "\n");
//					if( mHandler != null )
//					{
//						Message msg = mHandler.obtainMessage(99, "\n EPC encontrados: " + mTransponderCount + "\n");
//						mHandler.sendMessage(msg);
//					}
//					reportErrors(mReadCommand);
//					sendMessageNotification( String.format("Time taken: %.2fs", getTaskExecutionDuration()) );
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(99, "\n\n----------------------------------------------------------\n\n");
                        mHandler.sendMessage(msg);
                    }
//					sendMessageNotification("\n\n");
//					sendMessageNotification("-----------------------------");
//					sendMessageNotification("\n\n");

                }
            });

        } catch (ModelException e) {
//            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }

    }
}
