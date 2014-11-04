package fr.inria.soctrace.tools.ocelotl.ui.wrapper;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;

public class ComputePartWrapper {

	boolean	notEnded	= true;
	Object	synchronizator;

	public ComputePartWrapper(IProgressMonitor aMonitor, OcelotlCore aCore) {
		MonitorThread mThread = new MonitorThread(aMonitor);
		ComputePartThread cpThread = new ComputePartThread(aCore, mThread);
		
		mThread.setTheThread(cpThread);
		mThread.start();
		
		try {
			cpThread.join();
			mThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class MonitorThread extends Thread {
		private IProgressMonitor	monitor;
		private Thread theThread;
		
		public Thread getTheThread() {
			return theThread;
		}

		public void setTheThread(Thread theThread) {
			this.theThread = theThread;
		}

		public MonitorThread(IProgressMonitor aMonitor) {
			super();
			monitor = aMonitor;
		}

		@Override
		public void run() {

			while (notEnded) {
				if (monitor.isCanceled()) {
					synchronized (synchronizator) {
						notEnded = false;
						theThread.interrupt();
					}
					System.out.println("STOP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				}

				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Monitor interrupted!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					return;
				}
			}
		}
	}

	class ComputePartThread extends Thread {
		OcelotlCore		theCore;
		MonitorThread	mThread;

		public ComputePartThread(OcelotlCore aCore, MonitorThread anMThread) {
			super();
			theCore = aCore;
			mThread = anMThread;
			start();
		}

		@Override
		public void run() {
		//	theCore.computeParts();
			// Stop the monitoring thread
			interrupted();
			mThread.interrupt();
		}
	}

}
