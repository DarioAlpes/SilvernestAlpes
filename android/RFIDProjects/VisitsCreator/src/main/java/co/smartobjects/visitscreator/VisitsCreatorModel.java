//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package co.smartobjects.visitscreator;

import android.util.Pair;

import co.smartobjects.visitscreator.entities.*;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

import javax.net.ssl.HttpsURLConnection;

import co.smartobjects.visitscreator.utils.ModelBase;
import co.smartobjects.visitscreator.utils.ModelException;
import co.smartobjects.visitscreator.utils.services.POSTResponseProcessor;
import co.smartobjects.visitscreator.utils.services.RESTPoster;

public class VisitsCreatorModel extends ModelBase {

	// The instances used to issue commands
	private final ReadTransponderCommand mReadCommand = ReadTransponderCommand.synchronousCommand();

	// The inventory command configuration
	public ReadTransponderCommand getReadCommand() { return mReadCommand; }


	private int beaconsNumber;
	private Location location;
	private Sensor sensor;

	/**
	 * A class to demonstrate the use of the AsciiProtocol library to read and write to transponders
	 */
	public VisitsCreatorModel()
	{
		mReadCommand.setOffset(0);
		mReadCommand.setLength(1);
	}
    //----------------------------------------------------------------------------------------------
	// Read
	//----------------------------------------------------------------------------------------------

	// Set the parameters that are not user-specified
	private void setFixedReadParameters()
	{
		mReadCommand.setResetParameters(TriState.YES);

		// Configure the select to match the given EPC
		// EPC is in hex and length is in bits
		String epcHex = mReadCommand.getSelectData();

		if( epcHex == null || epcHex.length() == 0) {
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
				String dataMessage = ( data == null ) ? "No data!" : HexEncoding.bytesToString(data);
				String eaMsg = transponder.getAccessErrorCode() == null ? "" : "\n" + transponder.getAccessErrorCode().getDescription() + " (EA)";
				String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : "\n" + transponder.getBackscatterErrorCode().getDescription() + " (EB)";
				String errorMsg = eaMsg + ebMsg;
				if (errorMsg.length() > 0 ) {
					errorMsg = "Error: " + errorMsg + "\n";
				}
				String epc = transponder.getEpc();
				if(epc != null && epc.length() == 24) {
					String idClientStr = epc.substring(0,12);
					String idBeaconStr = epc.substring(12,24);
					try{
						long idClient = Long.parseLong(idClientStr, 16);
						long idBeacon = Long.parseLong(idBeaconStr, 16);
                        createReading(idClient, idBeacon);
						sendMessageNotification("Epc: "+ epc +"\n");
						sendMessageNotification("Cliente: "+ idClient +" (" + idClientStr + ")\n");
						sendMessageNotification("Se encontro el beacon con id: "+ idBeacon +" (" + idBeaconStr + ")\n");
						++beaconsNumber;
					}
					catch (NumberFormatException e){
						//Ignore errors
					}
				}

				if( !moreAvailable) {
					sendMessageNotification("\n");
				}
			}
		});
	}
	public void createVisit(long idClient, long idPerson) {
		if(location!=null) {
			Visit visitToPost = new Visit(idClient, idPerson, location.getId());
			new RESTPoster<>(new POSTResponseProcessor<Visit>() {
                @Override
                public void processPOSTResult(Visit visit, Pair<Integer, String> result) {
                    if(result == null) {
                        sendMessageNotification("Error creando la visita para la persona con id "+visit.getIdPerson()+".\n");
                    }
                    else if(result.first == HttpsURLConnection.HTTP_OK) {
                        sendMessageNotification("Visita creada para la persona con id "+visit.getIdPerson()+".\n");
                    }
                    else {
                        sendMessageNotification("Error creando la visita para la persona con id "+visit.getIdPerson()+", "+result.second+"\n");
                    }
                }
            }).execute(visitToPost);
		}
		else {
			sendMessageNotification("No se crean la visitas porque no hay ninguna ubicación seleccionada.");
		}
	}

	public void createReading(long idClient, long idBeacon) {
		if(sensor!=null) {
			Readings readings = new Readings(idClient, sensor.getId());
			readings.addReading(new Reading(idBeacon));
			new RESTPoster<>(new POSTResponseProcessor<Readings>() {
                @Override
                public void processPOSTResult(Readings readings, Pair<Integer, String> result) {
                    if(result == null) {
                        sendMessageNotification("Error creando la lectura para el sensor con id "+readings.getIdSensor()+".\n");
                    }
                    else if(result.first == HttpsURLConnection.HTTP_OK) {
                        sendMessageNotification("Lectura creada para el sensor con id "+readings.getIdSensor()+".\n");
                    }
                    else {
                        sendMessageNotification("Error creando la lectura para el sensor con id "+readings.getIdSensor()+", "+result.second+"\n");
                    }

                }
            }).execute(readings);
		}
		else {
			sendMessageNotification("No se crea la lectura porque no hay ninguna ubicación seleccionada.");
		}
	}




	public void read()
	{
		try {
			setFixedReadParameters();
            beaconsNumber = 0;

			performTask(new Runnable() {
				@Override
				public void run() {
				
					getCommander().executeCommand(mReadCommand);
					sendMessageNotification("Se encontro un total de " + beaconsNumber + " beacons.\n");
					reportErrors(mReadCommand);
					
				}
			});
			
		} catch (ModelException e) {
			sendMessageNotification("Unable to perform action: " + e.getMessage());
		}
	
	}

	/**
	 * Check the given command for errors and report them via the model message system
	 * @param command The command to check
	 */
	private void reportErrors(AsciiSelfResponderCommandBase command)
	{
		if( !command.isSuccessful() ) {
			sendMessageNotification(String.format(
					"%s failed!\nError code: %s\n", command.getClass().getSimpleName(), command.getErrorCode()));
			for (String message : command.getMessages()) {
				sendMessageNotification(message + "\n");
			}
		}
		
	}

	public void setLocation(Location location, Sensor sensor) {
		this.location = location;
        this.sensor = sensor;
	}
}
