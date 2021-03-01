package com.julind.esenseUtils.rotationDetector;

import android.util.Log;
import android.util.Pair;

import com.julind.esenseUtils.helperTools.HelperUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class RotationDetectorService {
    private static final String TAG = "RotationDetectorService";

    private final float movementDetectionDesensificationFactors;

    private final Deque<Double> xRotationsFifoList;
    private final Deque<Double> yRotationsFifoList;
    private final Deque<Double> zRotationsFifoList;

    private final int fifoListSize;
    private final int burstSize;

    private long lastMovementTimestamp;
    private final ArrayList<Pair<Movement, Double>> movementBurstBuffer;

    public RotationDetectorService(float movementDetectionDesensificationFactor,
                                   int burstSize) {
        this.movementDetectionDesensificationFactors = movementDetectionDesensificationFactor;

        this.burstSize = burstSize;
        this.fifoListSize = this.burstSize * 10;

        this.movementBurstBuffer =  new ArrayList<>(burstSize);

        this.xRotationsFifoList = new ArrayDeque<>(this.fifoListSize);
        this.yRotationsFifoList = new ArrayDeque<>(this.fifoListSize);
        this.zRotationsFifoList = new ArrayDeque<>(this.fifoListSize);
    }

    private Movement getDominantMovement() {
        int[] movements = new int[4];

        for (Pair<Movement, Double> movementDoublePair : this.movementBurstBuffer) {
            switch (movementDoublePair.first) {
                case ROTATION_LEFT:
                case TILT_LEFT:
                    movements[0]++;
                    break;
                case ROTATON_RIGHT:
                case TILT_RIGHT:
                    movements[1]++;
                    break;
                case ROTATION_UP:
                    movements[2]++;
                    break;
                case ROTATION_DOWN:
                    movements[3]++;
                    break;
            }
        }

        int indexOfLargest = HelperUtils.getIndexOfLargest(movements);

        if ((float) movements[indexOfLargest] / this.burstSize > 0.5) {
            switch (indexOfLargest) {
                case 0:
                    return Movement.ROTATION_LEFT;
                case 1:
                    return Movement.ROTATON_RIGHT;
                case 2:
                    return Movement.ROTATION_UP;
                case 3:
                    return Movement.ROTATION_DOWN;
            }
        }
        return Movement.NONE;
    }

    public Movement calculateMovement(double[] gyroMovement, long timestamp) {
        double xRotation = gyroMovement[0];
        double yRotation = gyroMovement[1];
        double zRotation = gyroMovement[2];

        if (this.xRotationsFifoList.size() < fifoListSize
                && this.yRotationsFifoList.size() < fifoListSize
                && this.zRotationsFifoList.size() < fifoListSize) {
            xRotationsFifoList.addFirst(xRotation);
            yRotationsFifoList.addFirst(yRotation);
            zRotationsFifoList.addFirst(zRotation);

            return Movement.TRAINING;
        }

        double absoluteXMean = HelperUtils.absoluteMean(xRotationsFifoList.toArray(new Double[0]));
        double absoluteYMean = HelperUtils.absoluteMean(yRotationsFifoList.toArray(new Double[0]));
        double absoluteZMean = HelperUtils.absoluteMean(zRotationsFifoList.toArray(new Double[0]));

        ArrayList<Pair<Movement, Double>> detectedMovements = new ArrayList<>(3);


        if (Math.abs(xRotation) > this.movementDetectionDesensificationFactors * absoluteXMean) {
            if (xRotation > 0f) {
                detectedMovements.add( new Pair<>(Movement.TILT_RIGHT, Math.abs(xRotation)));
            } else {
                detectedMovements.add(new Pair<>(Movement.TILT_LEFT, Math.abs(xRotation)));
            }
        }
        if (Math.abs(yRotation) > this.movementDetectionDesensificationFactors * absoluteYMean) {
            if (yRotation > 0f) {
                detectedMovements.add( new Pair<>(Movement.ROTATON_RIGHT, Math.abs(yRotation)));
            } else {
                detectedMovements.add(new Pair<>(Movement.ROTATION_LEFT, Math.abs(yRotation)));
            }
        }
        if (Math.abs(zRotation) > (this.movementDetectionDesensificationFactors - 1) * absoluteZMean) {
            if (zRotation > 0f) {
                detectedMovements.add(new Pair<>(Movement.ROTATION_DOWN, Math.abs(zRotation)));
            } else {
                detectedMovements.add(new Pair<>(Movement.ROTATION_UP, Math.abs(zRotation)));
            }
        }

        detectedMovements.sort((p1, p2) -> (-1) * p1.second.compareTo(p2.second));

        try {
            Pair<Movement, Double> movementDoublePair = detectedMovements.get(0);
            if (timestamp - this.lastMovementTimestamp > 1500) {
                if (this.movementBurstBuffer.size() >= this.burstSize) {
                    this.movementBurstBuffer.add(movementDoublePair);
                    Movement dominantDirection = this.getDominantMovement();
                    Log.i(TAG, "getDominantMovement: " + dominantDirection.toString());
                    this.movementBurstBuffer.clear();
                    this.lastMovementTimestamp = timestamp;
                    return dominantDirection;
                } else {
                    this.movementBurstBuffer.add(movementDoublePair);
                }
            } else {
                return Movement.NONE;
            }

        } catch (IndexOutOfBoundsException i) {
            this.movementBurstBuffer.clear();

            xRotationsFifoList.removeLast();
            yRotationsFifoList.removeLast();
            zRotationsFifoList.removeLast();

            xRotationsFifoList.addFirst(xRotation);
            yRotationsFifoList.addFirst(yRotation);
            zRotationsFifoList.addFirst(zRotation);
        }

        return Movement.NONE;
    }
}
