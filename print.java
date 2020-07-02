
    void openBT() throws IOException {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                Toast.makeText(List_cart.this, "No bluetooth adapter available", Toast.LENGTH_SHORT).show();
            }


            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);

                datalist.clear();
                datatotal.clear();
            }


            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    mmDevice = device;
                    UUID uuid = UUID.fromString(device.getUuids()[0].toString());

                    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

                    try {
                        mmSocket.connect();
                        mmOutputStream = mmSocket.getOutputStream();
                        mmInputStream = mmSocket.getInputStream();
                        beginListenForData();
                        notif_blue_true();
                    }
                    catch (Exception e) {

                        notif_blue_false();
                    }


                }
            }


        }catch(Exception e){
            e.printStackTrace();
        }




    }
    
    
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);



                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            public void run() {

                                            }
                                        });
                                }
                            }
                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            });
            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
