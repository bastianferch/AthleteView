db.createUser({
    user: 'mongousr',
    pwd: '1mongo3mongo3mongo7',
    roles: [
        {
            role: 'readWrite',
            db: 'athlete_view'
        }
    ]
})
