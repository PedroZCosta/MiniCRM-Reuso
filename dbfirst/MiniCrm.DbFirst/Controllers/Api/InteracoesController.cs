using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class InteracoesController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public InteracoesController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/Interacoes
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Interacao>>> GetInteracoes()
        {
            return await _context.Interacoes.ToListAsync();
        }

        // GET: api/Interacoes/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Interacao>> GetInteracao(int id)
        {
            var interacao = await _context.Interacoes.FindAsync(id);

            if (interacao == null)
            {
                return NotFound();
            }

            return interacao;
        }

        // PUT: api/Interacoes/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutInteracao(int id, Interacao interacao)
        {
            if (id != interacao.IdInteracao)
            {
                return BadRequest();
            }

            _context.Entry(interacao).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!InteracaoExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/Interacoes
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<Interacao>> PostInteracao(Interacao interacao)
        {
            _context.Interacoes.Add(interacao);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetInteracao", new { id = interacao.IdInteracao }, interacao);
        }

        // DELETE: api/Interacoes/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteInteracao(int id)
        {
            var interacao = await _context.Interacoes.FindAsync(id);
            if (interacao == null)
            {
                return NotFound();
            }

            _context.Interacoes.Remove(interacao);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool InteracaoExists(int id)
        {
            return _context.Interacoes.Any(e => e.IdInteracao == id);
        }
    }
}
