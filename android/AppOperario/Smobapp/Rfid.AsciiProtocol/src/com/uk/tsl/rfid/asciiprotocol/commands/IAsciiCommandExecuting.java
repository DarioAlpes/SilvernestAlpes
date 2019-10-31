package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.IAsciiCommandResponder;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Defines the responsibilities of classes that can execute and respond to a TSLAsciiCommand
 
 
 Responses to an executed TSLAsciiCommand (see executeCommand:) are handled through a responder chain (see responderChain)
 The responder chain is an ordered list of TSLAsciiCommandResponder that is traversed from the first responder added to the last.
 Each correctly terminated response line that has been received is passed to the TSLAsciiCommandResponder’s processReceivedLine: method.
 If a responder returns YES from the processReceivedLine method then the traversal ends otherwise it continues until all responders have been visited.
 
*/
public interface IAsciiCommandExecuting
{
	/** 
	 Gets the chain of TSLAsciiCommandResponders
	*/
	Iterable<IAsciiCommandResponder> getResponderChain();

	/** 
	 Add a responder to the responder chain
	 
	 @param responder The responder to add to the chain
	*/
	void addResponder(IAsciiCommandResponder responder);

	/** 
	 Add the synchronous responder into the chain
	 
	 
	 This is a special responder that despatches responses through a command’s synchronousCommandResponder property
	 There will only ever be one of these in the command chain
	 
	*/
	void addSynchronousResponder();

	/** 
	 Clear all responders from the responder chain
	*/
	void clearResponders();

	/** 
	 Execute the given command.
	 
	 @param command The command to be executed
	 
	 Command execution is asynchronous unless the command has a (non-nil) synchronousCommandResponder then
	 the command will be executed synchronously. Synchronous behaviour requires prior call to addSynchronousResponder.
	 Warning: derived classes must call the base implementation to ensure synchronous commands work correctly
	 
	*/
	void executeCommand(IAsciiCommand command);

	/** 
	 Remove a responder from the responder chain
	 
	 @param responder The responder to remove from the chain
	*/
	void removeResponder(IAsciiCommandResponder responder);

	/** 
	 Remove the synchronous responder from the chain
	*/
	void removeSynchronousResponder();
}