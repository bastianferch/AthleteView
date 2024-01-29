db.createUser({
    user: 'mongousr',
    pwd: 'fSA4STHqGmsqbqr9',
    roles: [
        {
            role: 'readWrite',
            db: 'athlete_view'
        }
    ]
})