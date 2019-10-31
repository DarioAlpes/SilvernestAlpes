import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Reservation } from './reservation';
import { ReservationService } from './reservation.service';
@Component({
  selector: 'reservation-detail',
  templateUrl: 'app/templates/packages/reservations/reservation-details.html'
})
export class ReservationDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    reservation: Reservation;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    idPackage: number;

    constructor(private reservationService: ReservationService, private route: ActivatedRoute, private router: Router)
    {
        this.reservation = new Reservation();
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
                        if(params['id'] !== undefined)
                        {
                            this.navigated = true;
                            let id = +params['id'];
                            this.reservationService.getReservation(this.idClient, this.idPackage, id)
                                .then(reservation => this.reservation = reservation)
                                .catch(error => this.error = error);
                        }
                        else
                        {
                            this.navigated = false;
                            this.reservation = new Reservation();
                        }
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

    saveReservation()
    {
        this.reservationService.saveReservation(this.idClient, this.idPackage, this.reservation)
            .then
                (
                    reservation =>
                    {
                        this.reservation = reservation;
                        this.goBack(reservation);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedReservation: Reservation = null)
    {
        this.close.emit(savedReservation);
        if (this.navigated)
        {
            window.history.back();
        }
    }

    ngOnDestroy()
    {
        this.subscriptions.unsubscribe();
    }
}
