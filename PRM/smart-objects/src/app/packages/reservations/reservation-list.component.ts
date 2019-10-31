import { Component, OnInit } from '@angular/core';
import { Reservation } from './reservation';
import { ReservationService } from './reservation.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'reservation-list',
  templateUrl: 'app/templates/packages/reservations/reservation-list.html'
})
export class ReservationListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private reservationService: ReservationService)
    { }

    selectedReservation: Reservation;

    reservations: Reservation[];

    showReservation = false;

    error: any;

    idClient: number;

    idPackage: number;

    subscriptions: any;

    onSelect(reservation: Reservation)
    {
        let link = ['/clients', this.idClient, '/packages', this.idPackage, '/reservations', reservation.id];
        this.router.navigate(link);
    }

    getReservations()
    {
        this.reservationService
        .getReservations(this.idClient, this.idPackage)
        .then(reservations => this.reservations = reservations)
        .catch(error => this.error = error);
    }

    addReservation()
    {
        this.showReservation = true;
        this.selectedReservation = new Reservation();
    }

    close(savedReservation: Reservation)
    {
        this.showReservation = false;
        if (savedReservation)
        {
            this.getReservations();
        }
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client'] !== undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id_package'] !== undefined)
                    {
                        this.idPackage = +params['id_package'];
                        this.getReservations();
                    }
                    else
                    {
                        this.router.navigate(['/clients', this.idClient, '/packages']);
                    }

                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
    }

    goBack()
    {
        window.history.back();
    }
}
