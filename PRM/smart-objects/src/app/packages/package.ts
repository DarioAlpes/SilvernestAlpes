export class Package
{
    public "historic-id": number;
    public id: number;
    public name: string;
    public price: number;
    public description: string;
    public "restricted-consumption": boolean = false;
    public "valid-from": string;
    public "valid-through": string;
    public duration: number;
    public "id-social-event": number;
    public "id-location": number;
}
