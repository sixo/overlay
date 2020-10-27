package eu.sisik.overlay


/**
 * Copyright (c) 2018 by Roman Sisik. All rights reserved.
 */


/**
 * Steering behaviours should simulate realistic character movement.
 * For more information see
 * https://gamedevelopment.tutsplus.com/series/understanding-steering-behaviors--gamedev-12732
 */
data class SteeringBehaviour(
    var speed: Float = 100f,
    var maxVelocity: Vec3 = Vec3(1f, 1f, 1f),
    var maxForce: Vec3 = Vec3(1f, 1f, 1f),
    var mass: Float = 1.0f,
    var velocity: Vec3 = Vec3(0f, 0f, 0f),
    var position: Vec3 = Vec3(0f, 0f, 0f)
) {


    fun seek(target: Vec3, deltaTime: Float) {

        var desiredVelocity = normalize(target - position) * maxVelocity
        var steering = desiredVelocity - velocity

        steering = truncate(steering, maxForce)
        steering = steering / mass

        velocity = truncate(velocity + steering, maxVelocity)
        position = position + velocity * (deltaTime * speed)

        // Some calculations might produce NaN. Here we simply
        // convert it to zero vector
        position = clampToZeroIfNan(position)
    }

    fun pursue(targetPosition: Vec3, targetVelocity: Vec3, deltaTime: Float) {

        var T = (targetPosition - position) / length(maxVelocity)
        var predictedPosition = targetPosition + targetVelocity * T
        seek(predictedPosition, deltaTime)
    }

    fun truncate(first: Vec3, second: Vec3): Vec3 {
        return clamp(first, length(second))
    }

    fun clampToZeroIfNan(vec: Vec3): Vec3 {

        if (position.x.isNaN() || position.y.isNaN() || position.z.isNaN())
            return  Vec3(0f, 0f, 0f)

        return vec
    }
}